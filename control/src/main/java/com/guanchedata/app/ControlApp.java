package com.guanchedata.app;

import com.guanchedata.api.ApiClient;
import com.guanchedata.config.AppConfig;
import com.guanchedata.core.ArgsParser;
import com.guanchedata.core.BookProcessor;
import com.guanchedata.core.Orchestrator;
import com.guanchedata.models.PipelineStage;
import com.guanchedata.ports.StatePort;
import com.guanchedata.state.StateStore;
import com.google.gson.Gson;

import java.io.IOException;

public class ControlApp {
    public void run(ArgsParser parser) throws Exception {
        AppConfig cfg = AppConfig.loadDefaults();
        StateStore store = new StateStore(parser.getStatePath(), new Gson());
        ApiClient http = new ApiClient();

        BookProcessor httpAdapter = new BookProcessor(http, cfg.ingestionBaseUrl, cfg.indexingBaseUrl);

        StatePort state = new StatePort() {
            @Override public void markIngesting(int bookId) throws IOException { store.setStage(bookId, PipelineStage.INGESTING); }
            @Override public void markIndexing(int bookId)  throws IOException { store.setStage(bookId, PipelineStage.INDEXING); }
            @Override public void markIndexed(int bookId)   throws IOException { store.setStage(bookId, PipelineStage.INDEXED); }
            @Override public void markError(int bookId, String errorCode) { store.setError(bookId, errorCode); }
            @Override public int size() { return store.size(); }
        };

        Orchestrator orchestrator = new Orchestrator(
                httpAdapter, httpAdapter, state, cfg.pollIntervalMs, cfg.pollTimeoutMs
        );

        for (int id : parser.getBookIds()) {
            try {
                orchestrator.processBook(id);
                System.out.println("{\"book_id\":" + id + ",\"status\":\"ok\"}");
            } catch (Exception e) {
                System.err.println("{\"book_id\":" + id + ",\"error\":\"" + e.getMessage().replace("\"","'") + "\"}");
            }
        }

        System.out.println("state_size=" + state.size());
    }
}
