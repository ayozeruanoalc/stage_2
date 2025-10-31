package com.guanchedata.util;

import com.guanchedata.application.usecases.control.Orchestrator;
import com.guanchedata.infrastructure.adapters.noname.RestConnector;
import com.guanchedata.infrastructure.adapters.noname.PipelineCoordinator;
import com.guanchedata.infrastructure.adapters.noname.PipelineStateManager;
import com.guanchedata.infrastructure.adapters.state.StateStore;
import com.guanchedata.infrastructure.ports.PipelineStateTracker;
import com.google.gson.Gson;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ControlRunner {

    public void run(String[] args) throws Exception {
        if (args == null || args.length < 2) {
            System.err.println("usage: java com.guanchedata.Main <state_file_path> <bookId> [<bookId> ...]");
            System.exit(1);
        }

        Path statePath = prepareStateFile(args[0]);
        List<Integer> bookIds = parseIds(args);

        String ingestionBaseUrl = env("INGESTION_BASE_URL", "http://localhost:7001");
        String indexingBaseUrl  = env("INDEXING_BASE_URL",  "http://localhost:7002");
        long pollIntervalMs     = Long.parseLong(env("POLL_INTERVAL_MS", "1000"));
        long pollTimeoutMs      = Long.parseLong(env("POLL_TIMEOUT_MS",  "600000"));

        StateStore store = new StateStore(statePath, new Gson());
        PipelineStateTracker state = new PipelineStateManager(store);

        RestConnector http = new RestConnector();
        PipelineCoordinator api = new PipelineCoordinator(http, ingestionBaseUrl, indexingBaseUrl);

        Orchestrator orchestrator = new Orchestrator(api, api, state, pollIntervalMs, pollTimeoutMs);

        for (int id : bookIds) {
            try {
                orchestrator.processBook(id);
                System.out.println("{\"book_id\":" + id + ",\"status\":\"ok\"}");
            } catch (Exception e) {
                System.err.println("{\"book_id\":" + id + ",\"error\":\"" + e.getMessage().replace("\"","'") + "\"}");
            }
        }
        System.out.println("state_size=" + state.size());
    }

    private static String env(String key, String def) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) v = System.getProperty(key, def);
        return v;
    }

    private static Path prepareStateFile(String p) throws Exception {
        Path file = Path.of(p);
        Path dir = file.getParent() == null ? Path.of(".") : file.getParent();
        Files.createDirectories(dir);
        return file;
    }

    private static List<Integer> parseIds(String[] args) {
        List<Integer> out = new ArrayList<>();
        for (int i = 1; i < args.length; i++) out.add(Integer.parseInt(args[i]));
        return out;
    }
}
