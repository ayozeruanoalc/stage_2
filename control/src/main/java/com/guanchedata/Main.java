package com.guanchedata;

import com.guanchedata.models.BookStatus;
import com.google.gson.Gson;
import io.javalin.Javalin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class Main {

    public static void main(String[] args) throws Exception {
        Map<String, String> cfg = Map.of(
                "CONTROL_PORT", getEnvOrDefault("CONTROL_PORT", "7003"),
                "INGESTION_BASE_URL", getEnvOrDefault("INGESTION_BASE_URL", "http://localhost:7001"),
                "INDEXING_BASE_URL", getEnvOrDefault("INDEXING_BASE_URL", "http://localhost:7002"),
                "SEARCH_BASE_URL", getEnvOrDefault("SEARCH_BASE_URL", "http://localhost:7000"),
                "STATE_FILE", getEnvOrDefault("STATE_FILE", "state/processed.json"),
                "POLL_INTERVAL_MS", getEnvOrDefault("POLL_INTERVAL_MS", "1000"),
                "POLL_TIMEOUT_MS", getEnvOrDefault("POLL_TIMEOUT_MS", "600000")
        );

        Path statePath = Path.of(cfg.get("STATE_FILE"));
        Files.createDirectories(statePath.getParent() == null ? Path.of(".") : statePath.getParent());

        Gson gson = new Gson();
        ApiClient client = new ApiClient();
        StateStore state = new StateStore(statePath, gson);
        Orchestrator orchestrator = new Orchestrator(
                client, state, gson,
                cfg.get("INGESTION_BASE_URL"),
                cfg.get("INDEXING_BASE_URL"),
                cfg.get("SEARCH_BASE_URL"),
                Integer.parseInt(cfg.get("POLL_INTERVAL_MS")),
                Integer.parseInt(cfg.get("POLL_TIMEOUT_MS"))
        );

        int port = Integer.parseInt(cfg.get("CONTROL_PORT"));
        Javalin app = Javalin.create(c -> c.http.defaultContentType = "application/json").start(port);

        app.get("/control/health", ctx ->
                ctx.json(Map.of("service", "control", "status", "running", "port", port))
        );

        app.post("/control/rebuild-index", ctx -> ctx.json(orchestrator.rebuildIndex()));

        app.post("/control/run/{bookId}", ctx -> {
            int bookId = Integer.parseInt(ctx.pathParam("bookId"));
            BookStatus status = orchestrator.processBook(bookId);
            ctx.json(Map.of("book_id", bookId, "status", status));
        });

        app.post("/control/run-batch", ctx -> {
            var body = gson.fromJson(ctx.body(), Map.class);
            List<Double> ids = (List<Double>) body.getOrDefault("book_ids", List.of());
            List<Integer> bookIds = ids.stream().map(Double::intValue).toList();
            var results = new ArrayList<Map<String, Object>>();
            for (int id : bookIds) {
                try {
                    BookStatus st = orchestrator.processBook(id);
                    results.add(Map.of("book_id", id, "status", st));
                } catch (Exception e) {
                    results.add(Map.of("book_id", id, "error", e.getMessage()));
                }
            }
            ctx.json(Map.of("count", results.size(), "results", results));
        });

        app.get("/control/processed", ctx ->
                ctx.json(Map.of("processed_count", state.size(), "book_ids", state.getAll()))
        );

        app.post("/control/processed/{bookId}", ctx -> {
            int bookId = Integer.parseInt(ctx.pathParam("bookId"));
            state.markProcessed(bookId);
            ctx.json(Map.of("book_id", bookId, "processed", true));
        });

        app.get("/control/pending", ctx -> {
            var ingested = orchestrator.listIngestedIds();
            var processed = new HashSet<>(state.getAll());
            var pending = ingested.stream().filter(id -> !processed.contains(id)).toList();
            ctx.json(Map.of("ingested", ingested, "processed", processed, "pending", pending));
        });
    }

    private static String getEnvOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }
}
