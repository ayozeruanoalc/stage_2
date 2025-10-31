package com.guanchedata.application.usecases.control;

import com.guanchedata.infrastructure.ports.BookIngestionController;
import com.guanchedata.infrastructure.ports.BookIndexController;
import com.guanchedata.infrastructure.ports.PipelineStateTracker;

public class Orchestrator {
    private final BookIngestionController ingestion;
    private final BookIndexController indexing;
    private final PipelineStateTracker state;
    private final long pollEveryMs;
    private final long pollTimeoutMs;

    public Orchestrator(BookIngestionController ingestion, BookIndexController indexing, PipelineStateTracker state,
                        long pollEveryMs, long pollTimeoutMs) {
        this.ingestion = ingestion;
        this.indexing = indexing;
        this.state = state;
        this.pollEveryMs = pollEveryMs;
        this.pollTimeoutMs = pollTimeoutMs;
    }

    public void processBook(int bookId) throws Exception {
        state.markIngesting(bookId);
        ingestion.triggerIngestion(bookId);

        long deadline = System.currentTimeMillis() + pollTimeoutMs;
        for (;;) {
            String s = ingestion.getStatus(bookId);
            if ("available".equalsIgnoreCase(s)) break;
            if (System.currentTimeMillis() >= deadline) {
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
