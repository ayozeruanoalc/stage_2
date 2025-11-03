package com.guanchedata.infrastructure.adapters.apiintegrator;

import com.guanchedata.domain.PipelineStage;
import com.guanchedata.infrastructure.ports.PipelineStateTracker;
import com.guanchedata.infrastructure.adapters.statestorer.StateStore;

import java.io.IOException;

public class PipelineStateManager implements PipelineStateTracker {
    private final StateStore store;

    public PipelineStateManager(StateStore store) {
        this.store = store;
    }

    @Override
    public void markIngesting(int bookId) throws IOException {
        store.setStage(bookId, PipelineStage.INGESTING);
    }

    @Override
    public void markIndexing(int bookId) throws IOException {
        store.setStage(bookId, PipelineStage.INDEXING);
    }

    @Override
    public void markIndexed(int bookId) throws IOException {
        store.setStage(bookId, PipelineStage.INDEXED);
    }

    @Override
    public void markError(int bookId, String errorCode) {
        store.setError(bookId, errorCode);
    }

    @Override
    public int size() {
        return store.size();
    }
}
