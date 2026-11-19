package com.example.survey.output;

import java.util.Locale;

/** 统计结果的输出格式枚举。 */
public enum ReportFormat {

    JSON, TEXT, CSV;

    public static ReportFormat parse(String raw) {
        if (raw == null || raw.isBlank()) {
            return JSON;
        }
        try {
            return ReportFormat.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("不支持的输出格式：" + raw + "（可选 json / text / csv）");
        }
    }

    public ReportFormatter formatter() {
        return switch (this) {
            case JSON -> new JsonFormatter();
            case TEXT -> new TextTableFormatter();
            case CSV -> new CsvFormatter();
        };
    }
}
