package com.guanchedata.core;

import com.guanchedata.api.ApiClient;
import com.guanchedata.state.StateStore;
import com.guanchedata.models.PipelineStage;
import com.google.gson.JsonObject;

import java.time.Duration;
import java.time.Instant;

public class IngestionMonitor {

    private final ApiClient http;
    private final StateStore state;
    private final int pollIntervalMs;
    private final int pollTimeoutMs;

    public IngestionMonitor(ApiClient http, StateStore state, int pollIntervalMs, int pollTimeoutMs) {
        this.http = http;
        this.state = state;
        this.pollIntervalMs = pollIntervalMs;
        this.pollTimeoutMs = pollTimeoutMs;
    }

    public String waitUntilDownloaded(int bookId, String ingestionBase) throws Exception {
        String statusUrl = ingestionBase + "/ingest/status/" + bookId;
        Instant start = Instant.now();

        while (true) {
            JsonObject st = http.getJson(statusUrl);
            String s = st.has("status") ? st.get("status").getAsString() : "unknown";

            if ("downloaded".equalsIgnoreCase(s) || "available".equalsIgnoreCase(s)) {
                state.setStage(bookId, PipelineStage.DOWNLOADED);
                return s;
            }

            if (Duration.between(start, Instant.now()).toMillis() > pollTimeoutMs) {
                state.setError(bookId, "ingestion-timeout");
                throw new RuntimeException("Timeout waiting ingestion " + bookId);
            }

            Thread.sleep(pollIntervalMs);
        }
    }
}
