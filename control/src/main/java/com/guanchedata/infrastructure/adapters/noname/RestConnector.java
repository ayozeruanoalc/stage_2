package com.guanchedata.infrastructure.adapters.noname;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

public class RestConnector {
    private final HttpClient client;
    private final Gson gson;
    private final Duration timeout;
    private final Map<String, String> defaultHeaders;

    public RestConnector() {
        this(Duration.ofSeconds(30), Map.of("Accept", "application/json", "Content-Type", "application/json"));
    }

    public RestConnector(Duration timeout, Map<String, String> defaultHeaders) {
        this.client = HttpClient.newBuilder().connectTimeout(timeout).build();
        this.gson = new Gson();
        this.timeout = timeout;
        this.defaultHeaders = defaultHeaders;
    }

    public JsonObject getJson(String url) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout)
                .GET();

        applyHeaders(builder);

        HttpResponse<String> resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        ensure2xx(resp);
        return parseJson(resp.body());
    }

    public JsonObject postJson(String url, JsonObject body) throws Exception {
        String payload = body == null ? "" : gson.toJson(body);
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString(payload));

        applyHeaders(builder);

        HttpResponse<String> resp = client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
        ensure2xx(resp);
        return parseJson(resp.body());
    }

    private void applyHeaders(HttpRequest.Builder builder) {
        if (defaultHeaders != null && !defaultHeaders.isEmpty()) {
            defaultHeaders.forEach(builder::header);
        }
    }

    private void ensure2xx(HttpResponse<?> resp) {
        int code = resp.statusCode();
        if (code < 200 || code >= 300) {
            String body = resp.body() == null ? "" : resp.body().toString();
            throw new RuntimeException("HTTP " + code + " -> " + body);
        }
    }

    private JsonObject parseJson(String body) {
        if (body == null || body.isBlank()) return new JsonObject();
        return gson.fromJson(body, JsonObject.class);
        }
}
