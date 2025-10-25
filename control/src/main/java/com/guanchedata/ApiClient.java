package com.guanchedata;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

import java.util.Map;

public class ApiClient {

    private static final int TIMEOUT_MILLIS = 5000;

    public JsonObject getJson(String url) throws Exception {
        Connection.Response resp = Jsoup.connect(url)
                .ignoreContentType(true)
                .timeout(TIMEOUT_MILLIS)
                .method(Connection.Method.GET)
                .header("Accept", "application/json")
                .execute();
        ensure2xx(resp.statusCode(), resp.body());
        return JsonParser.parseString(resp.body()).getAsJsonObject();
    }

    public JsonObject postJson(String url, String jsonBody) throws Exception {
        Connection conn = Jsoup.connect(url)
                .ignoreContentType(true)
                .timeout(TIMEOUT_MILLIS)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json")
                .requestBody(jsonBody == null ? "" : jsonBody)
                .method(Connection.Method.POST);

        Connection.Response resp = conn.execute();
        ensure2xx(resp.statusCode(), resp.body());
        return JsonParser.parseString(resp.body()).getAsJsonObject();
    }

    private static void ensure2xx(int statusCode, String body) {
        if (statusCode < 200 || statusCode >= 300) {
            throw new RuntimeException("HTTP " + statusCode + " – " + body);
        }
    }
}
