package com.guanchedata.infrastructure.adapters.state;

import com.guanchedata.domain.PipelineStage;
import com.google.gson.Gson;
import com.guanchedata.domain.StateEntry;

import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class StateStore {
    private final Map<Integer, StateEntry> states = new HashMap<>();
    private final StateFileManager fileManager;

    public StateStore(Path file, Gson gson) throws IOException {
        this.fileManager = new StateFileManager(file, gson);
        this.states.putAll(fileManager.load());
    }

    public int size() {
        return states.size();
    }

    public void setStage(int bookId, PipelineStage stage) throws IOException {
        if (stage == PipelineStage.LAUNCHED || stage == PipelineStage.INGESTING) {
            System.out.println("[STATE] Book " + bookId + " -> Stage: " + stage + " (not persisted yet)");
            return;
        }
        StateEntry e = states.getOrDefault(bookId, new StateEntry(bookId, null, null, null));
        e.setStage(stage.name());
        states.put(bookId, e);
        fileManager.save(states);
        System.out.println("[STATE] Book " + bookId + " -> Stage: " + stage + " (saved)");
    }

    public void setError(int bookId, String error) {
        System.err.println("[STATE] Book " + bookId + " -> ERROR: " + error);
    }
}
