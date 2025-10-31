package com.guanchedata.core;

import com.guanchedata.ports.IngestionPort;
import com.guanchedata.ports.IndexingPort;
import com.guanchedata.ports.StatePort;

public class Orchestrator {
    private final IngestionPort ingestion;
    private final IndexingPort indexing;
    private final StatePort state;
    private final long pollEveryMs;
    private final long pollTimeoutMs;

    public Orchestrator(IngestionPort ingestion, IndexingPort indexing, StatePort state, long pollEveryMs, long pollTimeoutMs) {
        this.ingestion = ingestion;
        this.indexing = indexing;
        this.state = state;
        this.pollEveryMs = pollEveryMs;
        this.pollTimeoutMs = pollTimeoutMs;
    }

    public void processBook(int bookId) throws Exception {
        state.markIngesting(bookId);
        ingestion.triggerIngestion(bookId);

        long start = System.currentTimeMillis();
        while (true) {
            String s = ingestion.getStatus(bookId);
            if ("available".equalsIgnoreCase(s)) break;
            if (System.currentTimeMillis() - start > pollTimeoutMs) {
                state.markError(bookId, "ingestion-timeout");
                throw new RuntimeException("Timeout waiting for ingestion: " + bookId);
            }
            Thread.sleep(pollEveryMs);
        }

        state.markIndexing(bookId);
        String idx = indexing.updateIndex(bookId);
        if (!"updated".equalsIgnoreCase(idx)) {
            state.markError(bookId, "indexing-" + idx);
            throw new RuntimeException("Indexing failed for " + bookId + " -> " + idx);
        }

        state.markIndexed(bookId);
    }
}
