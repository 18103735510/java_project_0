package com.example.survey;

import com.example.survey.api.QuestionnaireController;
import com.example.survey.api.StatisticsController;
import com.example.survey.api.SubmissionController;
import com.example.survey.common.ApiException;
import com.example.survey.repository.InMemoryQuestionnaireRepository;
import com.example.survey.repository.InMemorySubmissionRepository;
import com.example.survey.repository.QuestionnaireRepository;
import com.example.survey.repository.SubmissionRepository;
import com.example.survey.service.QuestionnaireService;
import com.example.survey.service.StatisticsService;
import com.example.survey.service.SubmissionService;
import com.example.survey.transport.HttpResult;
import com.example.survey.transport.Request;
import com.example.survey.transport.Router;
import com.example.survey.transport.StaticFiles;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Executors;

/**
 * 应用入口：手工装配依赖 -> 注册路由 -> 起 JDK 自带 HttpServer。
 * 不依赖 Spring / Tomcat，一个 main 方法拉起来就能跑。
 */
public final class App {

    private static final java.nio.charset.Charset UTF_8 = StandardCharsets.UTF_8;

    private App() {
    }

    public static void main(String[] args) throws Exception {
        Map<String, String> options = parseArgs(args);
        int port = Integer.parseInt(options.getOrDefault("port", "8080"));
        Path webRoot = Paths.get(options.getOrDefault("web", "web"));

        // ---------------- 依赖装配（纯手工，无框架） ----------------
        QuestionnaireRepository questionnaireRepository = new InMemoryQuestionnaireRepository();
        SubmissionRepository submissionRepository = new InMemorySubmissionRepository();

        QuestionnaireService questionnaireService = new QuestionnaireService(questionnaireRepository);
        SubmissionService submissionService = new SubmissionService(questionnaireRepository, submissionRepository);
        StatisticsService statisticsService = new StatisticsService(questionnaireRepository, submissionRepository);

        DemoData.seed(questionnaireService, submissionService);

        Router router = new Router();
        new QuestionnaireController(questionnaireService).register(router);
        new SubmissionController(submissionService).register(router);
        new StatisticsController(statisticsService).register(router);

        StaticFiles staticFiles = new StaticFiles(webRoot);

        // ---------------- 启动 HTTP 服务 ----------------
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", exchange -> handle(exchange, router, staticFiles));
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();

        System.out.println("==================================================");
        System.out.println(" 问卷调查统计系统（纯 Java 后端）已启动");
        System.out.println(" 地址：" + "http://127.0.0.1:" + port);
        System.out.println(" 前端目录：" + staticFiles.root());
        System.out.println(" 提示：Ctrl + C 结束进程");
        System.out.println("==================================================");
        if (!Files.isDirectory(staticFiles.root())) {
            System.out.println("[warn] 前端目录不存在，页面会 404，但接口仍可用：" + staticFiles.root());
        }
    }

    /* ------------------------------------------------------------------ 请求处理 */

    private static void handle(HttpExchange exchange, Router router, StaticFiles staticFiles) throws IOException {
        String method = exchange.getRequestMethod();
        String path = exchange.getRequestURI().getPath();
        long startedAt = System.nanoTime();

        HttpResult result;
        try {
            String body = new String(exchange.getRequestBody().readAllBytes(), UTF_8);
            Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
            Request request = new Request(method, path, query, Map.of(), body);

            Optional<HttpResult> routed = router.dispatch(request);
            if (routed.isPresent()) {
                result = routed.get();
            } else if ("GET".equalsIgnoreCase(method)) {
                result = staticFiles.serve(path)
                        .orElseGet(() -> HttpResult.error(404, "找不到资源：" + path));
            } else {
                result = HttpResult.error(404, "找不到接口：" + method + " " + path);
            }
        } catch (ApiException e) {
            result = HttpResult.error(e.status(), e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result = HttpResult.error(500, "服务器内部错误：" + e.getMessage());
        }

        write(exchange, result);
        long costMs = (System.nanoTime() - startedAt) / 1_000_000;
        System.out.printf("[%s] %s -> %d (%dms)%n", method, path, result.status(), costMs);
    }

    private static void write(HttpExchange exchange, HttpResult result) throws IOException {
        byte[] body = result.body() == null ? new byte[0] : result.body();
        exchange.getResponseHeaders().set("Content-Type", result.contentType());
        exchange.getResponseHeaders().set("Cache-Control", "no-store");
        if (body.length == 0) {
            exchange.sendResponseHeaders(result.status(), -1);
            exchange.close();
            return;
        }
        exchange.sendResponseHeaders(result.status(), body.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(body);
        }
    }

    /* ------------------------------------------------------------------ 工具 */

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> options = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("--") && arg.length() > 2) {
                String key = arg.substring(2);
                if (key.contains("=")) {
                    int split = key.indexOf('=');
                    options.put(key.substring(0, split), key.substring(split + 1));
                } else if (i + 1 < args.length && !args[i + 1].startsWith("--")) {
                    options.put(key, args[++i]);
                } else {
                    options.put(key, "true");
                }
            }
        }
        return options;
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> query = new HashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return query;
        }
        for (String pair : rawQuery.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int split = pair.indexOf('=');
            String key = split < 0 ? pair : pair.substring(0, split);
            String value = split < 0 ? "" : pair.substring(split + 1);
            query.put(URLDecoder.decode(key, UTF_8), URLDecoder.decode(value, UTF_8));
        }
        return query;
    }
}
