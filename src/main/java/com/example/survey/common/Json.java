package com.example.survey.common;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 极简 JSON 编解码器：只用 JDK，不引任何第三方库。
 *
 * <p>解析结果统一为：对象 -> {@code LinkedHashMap<String, Object>}，
 * 数组 -> {@code List<Object>}，整数 -> {@code Long}，小数 -> {@code Double}。
 * 序列化时保持插入顺序，方便输出"键序固定"的统计报告。</p>
 */
public final class Json {

    private Json() {
    }

    /* ------------------------------------------------------------------ 解析 */

    public static Object parse(String text) {
        if (text == null) {
            throw new IllegalArgumentException("JSON 文本为空");
        }
        Parser parser = new Parser(text);
        parser.skipWhitespace();
        Object value = parser.readValue();
        parser.skipWhitespace();
        if (!parser.atEnd()) {
            throw parser.error("末尾存在多余内容");
        }
        return value;
    }

    /* ------------------------------------------------------------------ 序列化 */

    public static String stringify(Object value) {
        return stringify(value, false);
    }

    public static String stringify(Object value, boolean pretty) {
        StringBuilder sb = new StringBuilder();
        write(value, sb, 0, pretty);
        return sb.toString();
    }

    private static void write(Object value, StringBuilder sb, int depth, boolean pretty) {
        if (value == null) {
            sb.append("null");
            return;
        }
        if (value instanceof String str) {
            writeString(str, sb);
            return;
        }
        if (value instanceof Boolean bool) {
            sb.append(bool.booleanValue());
            return;
        }
        if (value instanceof Double || value instanceof Float) {
            double d = ((Number) value).doubleValue();
            if (Double.isNaN(d) || Double.isInfinite(d)) {
                sb.append('0');
            } else if (d == Math.rint(d) && Math.abs(d) < 1e15) {
                sb.append((long) d);
            } else {
                sb.append(d);
            }
            return;
        }
        if (value instanceof Number number) {
            sb.append(number);
            return;
        }
        if (value instanceof Map<?, ?> map) {
            if (map.isEmpty()) {
                sb.append("{}");
                return;
            }
            sb.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                if (pretty) {
                    newline(sb, depth + 1);
                }
                writeString(String.valueOf(entry.getKey()), sb);
                sb.append(pretty ? ": " : ":");
                write(entry.getValue(), sb, depth + 1, pretty);
            }
            if (pretty) {
                newline(sb, depth);
            }
            sb.append('}');
            return;
        }
        if (value instanceof Iterable<?> iterable) {
            sb.append('[');
            boolean first = true;
            for (Object item : iterable) {
                if (!first) {
                    sb.append(',');
                }
                first = false;
                if (pretty) {
                    newline(sb, depth + 1);
                }
                write(item, sb, depth + 1, pretty);
            }
            if (pretty && !first) {
                newline(sb, depth);
            }
            sb.append(']');
            return;
        }
        writeString(String.valueOf(value), sb);
    }

    private static void newline(StringBuilder sb, int depth) {
        sb.append('\n');
        sb.append("  ".repeat(depth));
    }

    private static void writeString(String str, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                case '\b' -> sb.append("\\b");
                case '\f' -> sb.append("\\f");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }

    /* ------------------------------------------------------------------ 取值助手 */

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObject(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        throw new IllegalArgumentException("期望 JSON 对象");
    }

    @SuppressWarnings("unchecked")
    public static List<Object> asArray(Object value) {
        if (value instanceof List<?> list) {
            return (List<Object>) list;
        }
        throw new IllegalArgumentException("期望 JSON 数组");
    }

    public static List<Object> asArrayOrEmpty(Object value) {
        return value instanceof List<?> list ? asArray(list) : List.of();
    }

    public static String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    public static String str(Object value, String fallback) {
        return value == null ? fallback : String.valueOf(value);
    }

    public static Integer intOrNull(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        try {
            return Integer.valueOf(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("期望整数：" + value);
        }
    }

    public static long asLong(Object value, long fallback) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        if (value == null) {
            return fallback;
        }
        try {
            return Long.parseLong(String.valueOf(value).trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    /* ------------------------------------------------------------------ 递归下降解析器 */

    private static final class Parser {

        private final String text;
        private int index;

        Parser(String text) {
            this.text = text;
        }

        int position() {
            return index;
        }

        boolean atEnd() {
            return index >= text.length();
        }

        void skipWhitespace() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
        }

        Object readValue() {
            skipWhitespace();
            if (atEnd()) {
                throw error("内容意外结束");
            }
            char c = text.charAt(index);
            return switch (c) {
                case '{' -> readObject();
                case '[' -> readArray();
                case '"' -> readString();
                case 't' -> readLiteral("true", Boolean.TRUE);
                case 'f' -> readLiteral("false", Boolean.FALSE);
                case 'n' -> readLiteral("null", null);
                default -> {
                    if (c == '-' || (c >= '0' && c <= '9')) {
                        yield readNumber();
                    }
                    throw error("无法识别的字符 '" + c + "'");
                }
            };
        }

        Map<String, Object> readObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            expect('{');
            skipWhitespace();
            if (peek() == '}') {
                index++;
                return map;
            }
            while (true) {
                skipWhitespace();
                String key = readString();
                skipWhitespace();
                expect(':');
                map.put(key, readValue());
                skipWhitespace();
                char c = next();
                if (c == '}') {
                    return map;
                }
                if (c != ',') {
                    throw error("对象中期望 ',' 或 '}'");
                }
            }
        }

        List<Object> readArray() {
            List<Object> list = new ArrayList<>();
            expect('[');
            skipWhitespace();
            if (peek() == ']') {
                index++;
                return list;
            }
            while (true) {
                list.add(readValue());
                skipWhitespace();
                char c = next();
                if (c == ']') {
                    return list;
                }
                if (c != ',') {
                    throw error("数组中期望 ',' 或 ']'");
                }
            }
        }

        String readString() {
            expect('"');
            StringBuilder sb = new StringBuilder();
            while (true) {
                if (atEnd()) {
                    throw error("字符串未闭合");
                }
                char c = text.charAt(index++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c != '\\') {
                    sb.append(c);
                    continue;
                }
                if (atEnd()) {
                    throw error("转义符未闭合");
                }
                char escaped = text.charAt(index++);
                switch (escaped) {
                    case '"' -> sb.append('"');
                    case '\\' -> sb.append('\\');
                    case '/' -> sb.append('/');
                    case 'b' -> sb.append('\b');
                    case 'f' -> sb.append('\f');
                    case 'n' -> sb.append('\n');
                    case 'r' -> sb.append('\r');
                    case 't' -> sb.append('\t');
                    case 'u' -> {
                        if (index + 4 > text.length()) {
                            throw error("\\u 转义不完整");
                        }
                        String hex = text.substring(index, index + 4);
                        index += 4;
                        try {
                            sb.append((char) Integer.parseInt(hex, 16));
                        } catch (NumberFormatException e) {
                            throw error("非法的 \\u 转义：" + hex);
                        }
                    }
                    default -> throw error("非法的转义字符：\\" + escaped);
                }
            }
        }

        Object readNumber() {
            int start = index;
            while (index < text.length()) {
                char c = text.charAt(index);
                if ((c >= '0' && c <= '9') || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E') {
                    index++;
                } else {
                    break;
                }
            }
            String raw = text.substring(start, index);
            try {
                if (raw.contains(".") || raw.contains("e") || raw.contains("E")) {
                    return Double.parseDouble(raw);
                }
                return Long.parseLong(raw);
            } catch (NumberFormatException e) {
                throw error("非法的数字：" + raw);
            }
        }

        Object readLiteral(String literal, Object value) {
            if (!text.startsWith(literal, index)) {
                throw error("期望字面量 " + literal);
            }
            index += literal.length();
            return value;
        }

        char peek() {
            return index < text.length() ? text.charAt(index) : '\0';
        }

        char next() {
            if (atEnd()) {
                throw error("内容意外结束");
            }
            return text.charAt(index++);
        }

        void expect(char expected) {
            skipWhitespace();
            char c = next();
            if (c != expected) {
                throw error("期望 '" + expected + "'，实际 '" + c + "'");
            }
        }

        IllegalArgumentException error(String message) {
            return new IllegalArgumentException("JSON 解析失败（位置 " + index + "）：" + message);
        }
    }
}
