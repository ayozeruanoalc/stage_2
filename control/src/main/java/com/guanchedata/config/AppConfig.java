package com.guanchedata.config;

public class AppConfig {
    public final String ingestionBaseUrl;
    public final String indexingBaseUrl;
    public final String searchBaseUrl;
    public final int pollIntervalMs;
    public final int pollTimeoutMs;

    private AppConfig(String ingestionBaseUrl, String indexingBaseUrl, String searchBaseUrl,
                      int pollIntervalMs, int pollTimeoutMs) {
        this.ingestionBaseUrl = ingestionBaseUrl;
        this.indexingBaseUrl = indexingBaseUrl;
        this.searchBaseUrl = searchBaseUrl;
        this.pollIntervalMs = pollIntervalMs;
        this.pollTimeoutMs = pollTimeoutMs;
    }

    public static AppConfig loadDefaults() {
        return new AppConfig(
                "http://localhost:7001",
                "http://localhost:7002",
                "http://localhost:7000",
                1000,
                600_000
        );
    }
}
