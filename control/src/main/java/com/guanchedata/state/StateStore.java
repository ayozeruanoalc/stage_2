package com.guanchedata.state;

import com.guanchedata.models.PipelineStage;
import com.google.gson.Gson;

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

    public synchronized boolean isProcessed(int id) {
        StateEntry e = states.get(id);
        return e != null && ("INDEXED".equalsIgnoreCase(e.getStage()) || "DONE".equalsIgnoreCase(e.getStage()));
    }

    public synchronized int size() {
        return states.size();
    }

    public synchronized void setStage(int bookId, PipelineStage stage) throws IOException {
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

    public synchronized void setPath(int bookId, String path) throws IOException {
        StateEntry e = states.getOrDefault(bookId, new StateEntry(bookId, null, null, null));
        e.setPath(path);
        states.put(bookId, e);
        fileManager.save(states);
        System.out.println("[STATE] Book " + bookId + " -> Path: " + path);
    }

    public synchronized void setError(int bookId, String error) {
        System.err.println("[STATE] Book " + bookId + " -> ERROR: " + error);
    }
}
