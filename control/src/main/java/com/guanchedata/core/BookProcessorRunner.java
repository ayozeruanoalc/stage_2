package com.guanchedata.core;

import com.guanchedata.api.ApiClient;
import com.guanchedata.state.StateStore;
import com.guanchedata.config.AppConfig;
import com.guanchedata.models.BookStatus;
import com.google.gson.Gson;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class BookProcessorRunner {

    private final AppConfig config;
    private final ArgsParser parser;

    public BookProcessorRunner(AppConfig config, ArgsParser parser) {
        this.config = config;
        this.parser = parser;
    }

    public void run() throws Exception {
        Gson gson = new Gson();
        ApiClient client = new ApiClient();
        Path statePath = parser.getStatePath();
        List<Integer> ids = parser.getBookIds();

        StateStore state = new StateStore(statePath, gson);
        Orchestrator orchestrator = new Orchestrator(
                client, state, gson,
                config.ingestionBaseUrl,
                config.indexingBaseUrl,
                config.searchBaseUrl,
                config.pollIntervalMs,
                config.pollTimeoutMs
        );

        System.out.println("Number of books to process: " + ids.size());

        for (int id : ids) {
            try {
                BookStatus st = orchestrator.processBook(id);
                System.out.println(gson.toJson(Map.of("book_id", id, "status", st)));
            } catch (Exception e) {
                System.err.println(gson.toJson(Map.of("book_id", id, "error", e.getMessage())));
            }
        }

        System.out.println("Total books processed: " + state.size());
    }
}
