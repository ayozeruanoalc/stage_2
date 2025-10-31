package com.guanchedata.infrastructure.adapters.noname;

import com.guanchedata.infrastructure.ports.BookIngestionController;
import com.guanchedata.infrastructure.ports.BookIndexController;
import com.google.gson.JsonObject;

public class PipelineCoordinator implements BookIngestionController, BookIndexController {
    private final RestConnector http;
    private final String ingestionBase;
    private final String indexingBase;

    public PipelineCoordinator(RestConnector http, String ingestionBase, String indexingBase) {
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
        if (resp != null && resp.has("status") && !resp.get("status").isJsonNull()) {
            return resp.get("status").getAsString();
        }
        return "unknown";
    }

    @Override
    public String updateIndex(int bookId) throws Exception {
        JsonObject resp = http.postJson(indexingBase + "/index/update/" + bookId, null);
        if (resp != null && resp.has("index") && !resp.get("index").isJsonNull()) {
            return resp.get("index").getAsString();
        }
        return "failed";
    }
}
