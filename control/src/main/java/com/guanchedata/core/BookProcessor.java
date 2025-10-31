package com.guanchedata.core;

import com.guanchedata.api.ApiClient;
import com.guanchedata.ports.IngestionPort;
import com.guanchedata.ports.IndexingPort;
import com.google.gson.JsonObject;

public class BookProcessor implements IngestionPort, IndexingPort {
    private final ApiClient http;
    private final String ingestionBase;
    private final String indexingBase;

    public BookProcessor(ApiClient http, String ingestionBase, String indexingBase) {
        this.http = http;
        this.ingestionBase = ingestionBase;
        this.indexingBase = indexingBase;
    }

    @Override
    public void triggerIngestion(int bookId) throws Exception {
        http.postJson(ingestionBase + "/ingest/" + bookId, null);
    }

    @Override
    public String getStatus(int bookId) throws Exception {
        JsonObject resp = http.getJson(ingestionBase + "/ingest/status/" + bookId);
        if (resp.has("status") && !resp.get("status").isJsonNull()) return resp.get("status").getAsString();
        return "unknown";
    }

    @Override
    public String updateIndex(int bookId) throws Exception {
        JsonObject resp = http.postJson(indexingBase + "/index/update/" + bookId, null);
        if (resp.has("index") && !resp.get("index").isJsonNull()) return resp.get("index").getAsString();
        return "failed";
    }
}
