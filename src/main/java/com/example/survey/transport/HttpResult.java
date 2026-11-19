package com.example.survey.transport;

import com.example.survey.common.Json;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** 一次 HTTP 响应的结果对象。 */
public record HttpResult(int status, String contentType, byte[] body) {

    private static final java.nio.charset.Charset UTF_8 = StandardCharsets.UTF_8;

    /** 成功响应：{"ok":true,"data":...} */
    public static HttpResult data(Object payload) {
        return json(200, envelope(payload));
    }

    /** 创建成功：HTTP 201 + 同样的信封。 */
    public static HttpResult created(Object payload) {
        return json(201, envelope(payload));
    }

    /** 错误响应：{"ok":false,"status":400,"error":"..."} */
    public static HttpResult error(int status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", false);
        body.put("status", status);
        body.put("error", message);
        return json(status, body);
    }

    public static HttpResult json(int status, Object payload) {
        return new HttpResult(status, "application/json; charset=utf-8",
                Json.stringify(payload, true).getBytes(UTF_8));
    }

    /** 原样返回字符串（统计报告的 text / csv 走这里，不套信封）。 */
    public static HttpResult raw(String contentType, String text) {
        return new HttpResult(200, contentType, text.getBytes(UTF_8));
    }

    public static HttpResult text(int status, String text) {
        return new HttpResult(status, "text/plain; charset=utf-8", text.getBytes(UTF_8));
    }

    public static HttpResult bytes(int status, String contentType, byte[] payload) {
        return new HttpResult(status, contentType, payload);
    }

    private static Map<String, Object> envelope(Object payload) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("data", payload);
        return body;
    }
}
