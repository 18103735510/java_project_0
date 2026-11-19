package com.example.survey.transport;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Optional;

/** 静态文件服务：把 web 目录下的 HTML 直接吐给浏览器。 */
public final class StaticFiles {

    private final Path root;

    public StaticFiles(Path root) {
        this.root = root.toAbsolutePath().normalize();
    }

    public Path root() {
        return root;
    }

    public Optional<HttpResult> serve(String path) {
        String relative = (path == null || path.isBlank() || "/".equals(path)) ? "index.html" : path.substring(1);
        Path file = root.resolve(relative).normalize();
        if (!file.startsWith(root)) {
            return Optional.of(HttpResult.text(403, "forbidden"));
        }
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try {
            return Optional.of(HttpResult.bytes(200, contentType(relative), Files.readAllBytes(file)));
        } catch (IOException e) {
            return Optional.of(HttpResult.text(500, "读取静态文件失败：" + e.getMessage()));
        }
    }

    private static String contentType(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        if (lower.endsWith(".html") || lower.endsWith(".htm")) {
            return "text/html; charset=utf-8";
        }
        if (lower.endsWith(".css")) {
            return "text/css; charset=utf-8";
        }
        if (lower.endsWith(".js")) {
            return "application/javascript; charset=utf-8";
        }
        if (lower.endsWith(".json")) {
            return "application/json; charset=utf-8";
        }
        if (lower.endsWith(".svg")) {
            return "image/svg+xml";
        }
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        return "application/octet-stream";
    }
}
