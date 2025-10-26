package com.guanchedata.core;

import com.guanchedata.api.ApiClient;
import com.guanchedata.state.StateStore;
import com.guanchedata.models.BookStatus;
import com.guanchedata.models.PipelineStage;
import com.google.gson.JsonObject;

public class BookProcessor {

    private final ApiClient http;
    private final StateStore state;
    private final IngestionMonitor monitor;

    private final String ingestionBase;
    private final String indexingBase;

    public BookProcessor(ApiClient http, StateStore state, IngestionMonitor monitor,
                         String ingestionBase, String indexingBase) {
        this.http = http;
        this.state = state;
        this.monitor = monitor;
        this.ingestionBase = ingestionBase;
        this.indexingBase = indexingBase;
    }

    public BookStatus process(int bookId) throws Exception {
        BookStatus status = new BookStatus(bookId);

        if (state.isProcessed(bookId)) {
            status.ingestionStatus = "skipped_already_processed";
            status.indexingStatus = "skipped";
            return status;
        }

        state.setStage(bookId, PipelineStage.LAUNCHED);

        String ingestUrl = ingestionBase + "/ingest/" + bookId;
        state.setStage(bookId, PipelineStage.INGESTING);
        JsonObject ingestResp = http.postJson(ingestUrl, null);
        status.ingestionStatus = getStringOr(ingestResp, "status", "requested");
        status.path = getStringOr(ingestResp, "path", null);
        if (status.path != null) state.setPath(bookId, status.path);

        status.ingestionStatus = monitor.waitUntilDownloaded(bookId, ingestionBase);

        if ("not_available".equalsIgnoreCase(status.ingestionStatus) ||
                "error".equalsIgnoreCase(status.ingestionStatus)) {
            System.out.println("Skipping indexing for book " + bookId + " (ingestion failed).");
            state.setError(bookId, "ingestion-" + status.ingestionStatus.toLowerCase());
            return status;
        }

        String indexUrl = indexingBase + "/index/update/" + bookId;
        state.setStage(bookId, PipelineStage.INDEXING);
        JsonObject idxResp = http.postJson(indexUrl, null);
        String idx = getStringOr(idxResp, "index", "unknown");
        if (!"updated".equalsIgnoreCase(idx)) {
            state.setError(bookId, "indexing-" + idx);
            throw new RuntimeException("Indexing not completed for " + bookId + " (index=" + idx + ")");
        }

        status.indexingStatus = idx;
        state.setStage(bookId, PipelineStage.INDEXED);
        return status;
    }

    private static String getStringOr(JsonObject obj, String key, String def) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return def;
        return obj.get(key).getAsString();
    }
}
