package com.example.survey.transport;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 极简路由器：按「方法 + 路径模式」匹配，支持 {name} 形式的路径参数。
 * 模式示例：GET /api/questionnaires/{id}/statistics
 */
public final class Router {

    @FunctionalInterface
    public interface Handler {
        HttpResult handle(Request request) throws Exception;
    }

    private record Route(String method, String[] segments, Handler handler) {
    }

    private final List<Route> routes = new ArrayList<>();

    public void get(String pattern, Handler handler) {
        add("GET", pattern, handler);
    }

    public void post(String pattern, Handler handler) {
        add("POST", pattern, handler);
    }

    private void add(String method, String pattern, Handler handler) {
        routes.add(new Route(method, segments(pattern), handler));
    }

    public Optional<HttpResult> dispatch(Request request) throws Exception {
        String[] actual = segments(request.path());
        for (Route route : routes) {
            if (!route.method().equalsIgnoreCase(request.method())) {
                continue;
            }
            Map<String, String> params = match(route.segments(), actual);
            if (params == null) {
                continue;
            }
            return Optional.of(route.handler().handle(request.withPathParams(params)));
        }
        return Optional.empty();
    }

    private static String[] segments(String path) {
        String trimmed = path == null ? "" : path.trim();
        while (trimmed.startsWith("/")) {
            trimmed = trimmed.substring(1);
        }
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed.isEmpty() ? new String[0] : trimmed.split("/");
    }

    private static Map<String, String> match(String[] pattern, String[] actual) {
        if (pattern.length != actual.length) {
            return null;
        }
        Map<String, String> params = new LinkedHashMap<>();
        for (int i = 0; i < pattern.length; i++) {
            String expected = pattern[i];
            if (expected.startsWith("{") && expected.endsWith("}")) {
                params.put(expected.substring(1, expected.length() - 1), actual[i]);
                continue;
            }
            if (!expected.equals(actual[i])) {
                return null;
            }
        }
        return params;
    }
}
