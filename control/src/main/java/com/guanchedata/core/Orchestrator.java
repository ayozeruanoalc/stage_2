package com.guanchedata.core;

import com.guanchedata.api.ApiClient;
import com.guanchedata.state.StateStore;
import com.guanchedata.models.BookStatus;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

public class Orchestrator {

    private final ApiClient http;
    private final StateStore state;
    private final Gson gson;

    private final String ingestionBase;
    private final String indexingBase;
    private final String searchBase;

    private final BookProcessor processor;

    public Orchestrator(ApiClient http, StateStore state, Gson gson,
                        String ingestionBase, String indexingBase, String searchBase,
                        int pollIntervalMs, int pollTimeoutMs) {

        this.http = http;
        this.state = state;
        this.gson = gson;
        this.ingestionBase = ingestionBase;
        this.indexingBase = indexingBase;
        this.searchBase = searchBase;

        IngestionMonitor monitor = new IngestionMonitor(http, state, pollIntervalMs, pollTimeoutMs);
        this.processor = new BookProcessor(http, state, monitor, ingestionBase, indexingBase);
    }

    public BookStatus processBook(int bookId) throws Exception {
        return processor.process(bookId);
    }

    public Map<String, Object> rebuildIndex() throws Exception {
        String url = indexingBase + "/index/rebuild";
        JsonObject resp = http.postJson(url, null);
        return gson.fromJson(resp, Map.class);
    }

    public List<Integer> listIngestedIds() throws Exception {
        String url = ingestionBase + "/ingest/list";
        JsonObject resp = http.getJson(url);
        JsonElement books = resp.get("books");
        return books == null || !books.isJsonArray()
                ? List.of()
                : books.getAsJsonArray().asList().stream().map(JsonElement::getAsInt).toList();
    }
}
