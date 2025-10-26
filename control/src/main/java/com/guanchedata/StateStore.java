package com.guanchedata;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class StateStore {

    private final Path file;
    private final Gson gson;
    private final Set<Integer> processed = new HashSet<>();
    private static final Type SET_TYPE = new TypeToken<Set<Integer>>(){}.getType();

    public StateStore(Path file, Gson gson) throws IOException {
        this.file = file;
        this.gson = gson;
        load();
    }

    public synchronized boolean isProcessed(int id) {
        return processed.contains(id);
    }

    public synchronized void markProcessed(int id) throws IOException {
        processed.add(id);
        save();
    }

    public synchronized List<Integer> getAll() {
        return processed.stream().sorted().toList();
    }

    public synchronized int size() {
        return processed.size();
    }

    private void load() throws IOException {
        if (!Files.exists(file)) {
            save();
            return;
        }
        String content = Files.readString(file);
        if (content == null || content.isBlank()) return;
        Set<Integer> loaded = gson.fromJson(content, SET_TYPE);
        if (loaded != null) processed.addAll(loaded);
    }

    private void save() throws IOException {
        if (!Files.exists(file.getParent())) {
            if (file.getParent() != null) Files.createDirectories(file.getParent());
        }
        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            w.write(gson.toJson(processed, SET_TYPE));
        }
    }
}
