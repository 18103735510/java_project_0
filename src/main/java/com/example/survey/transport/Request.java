package com.example.survey.transport;

import com.example.survey.common.ApiException;
import com.example.survey.common.Json;

import java.util.Map;

/** 规范化后的请求对象：控制器只跟它打交道，不碰 HttpExchange。 */
public record Request(String method, String path, Map<String, String> query,
                      Map<String, String> pathParams, String body) {

    public Request {
        query = query == null ? Map.of() : Map.copyOf(query);
        pathParams = pathParams == null ? Map.of() : Map.copyOf(pathParams);
    }

    public Request withPathParams(Map<String, String> params) {
        return new Request(method, path, query, params, body);
    }

    public String query(String name) {
        return query.get(name);
    }

    public String query(String name, String fallback) {
        String value = query.get(name);
        return value == null || value.isBlank() ? fallback : value;
    }

    public String pathParam(String name) {
        String value = pathParams.get(name);
        if (value == null) {
            throw new IllegalStateException("缺少路径参数：" + name);
        }
        return value;
    }

    public long pathParamLong(String name) {
        try {
            return Long.parseLong(pathParam(name));
        } catch (NumberFormatException e) {
            throw ApiException.badRequest("路径参数必须是数字：" + name);
        }
    }

    public Map<String, Object> jsonObject() {
        if (body == null || body.isBlank()) {
            throw ApiException.badRequest("请求体不能为空");
        }
        try {
            return Json.asObject(Json.parse(body));
        } catch (IllegalArgumentException e) {
            throw ApiException.badRequest(e.getMessage());
        }
    }
}
