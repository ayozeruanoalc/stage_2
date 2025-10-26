package com.guanchedata;

import com.guanchedata.models.BookStatus;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

public class Orchestrator {

    private final ApiClient http;
    private final StateStore state;
    private final Gson gson;

    private final String ingestionBase;
    private final String indexingBase;
    private final String searchBase;
    private final int pollIntervalMs;
    private final int pollTimeoutMs;

    public Orchestrator(ApiClient http, StateStore state, Gson gson,
                        String ingestionBase, String indexingBase, String searchBase,
                        int pollIntervalMs, int pollTimeoutMs) {
        this.http = http;
        this.state = state;
        this.gson = gson;
        this.ingestionBase = ingestionBase;
        this.indexingBase = indexingBase;
        this.searchBase = searchBase;
        this.pollIntervalMs = pollIntervalMs;
        this.pollTimeoutMs = pollTimeoutMs;
    }

    public BookStatus processBook(int bookId) throws Exception {
        BookStatus status = new BookStatus(bookId);

        if (state.isProcessed(bookId)) {
            status.ingestionStatus = "skipped_already_processed";
            status.indexingStatus = "skipped";
            return status;
        }

        String ingestUrl = ingestionBase + "/ingest/" + bookId;
        JsonObject ingestResp = http.postJson(ingestUrl, null);
        status.ingestionStatus = getStringOr(ingestResp, "status", "requested");
        status.path = getStringOr(ingestResp, "path", null);

        String statusUrl = ingestionBase + "/ingest/status/" + bookId;
        Instant start = Instant.now();
        while (true) {
            JsonObject st = http.getJson(statusUrl);
            String s = getStringOr(st, "status", "unknown");
            if ("available".equalsIgnoreCase(s) || "downloaded".equalsIgnoreCase(s)) {
                status.ingestionStatus = s;
                break;
            }
            if (Duration.between(start, Instant.now()).toMillis() > pollTimeoutMs) {
                throw new RuntimeException("⏰ Timeout: Ingestion took too long for book " + bookId);
            }
            Thread.sleep(pollIntervalMs);
        }

        String indexUrl = indexingBase + "/index/update/" + bookId;
        JsonObject idxResp = http.postJson(indexUrl, null);
        status.indexingStatus = getStringOr(idxResp, "index", "requested");

        state.markProcessed(bookId);
        return status;
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

    private static String getStringOr(JsonObject obj, String key, String def) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) return def;
        return obj.get(key).getAsString();
    }
}
