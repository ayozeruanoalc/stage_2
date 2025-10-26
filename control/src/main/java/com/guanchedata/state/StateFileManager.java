package com.guanchedata.state;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class StateFileManager {

    private final Path file;
    private final Gson gson;
    private static final Type MAP_TYPE = new TypeToken<Map<Integer, StateEntry>>(){}.getType();

    public StateFileManager(Path file, Gson gson) {
        this.file = file;
        this.gson = gson;
    }

    public Map<Integer, StateEntry> load() throws IOException {
        if (!Files.exists(file)) {
            Files.createDirectories(file.getParent());
            try (BufferedWriter w = Files.newBufferedWriter(file)) {
                w.write("{}");
            }
            return new HashMap<>();
        }

        String content = Files.readString(file).trim();
        if (content.isEmpty()) return new HashMap<>();

        try {
            Map<Integer, StateEntry> loaded = gson.fromJson(content, MAP_TYPE);
            return (loaded != null) ? loaded : new HashMap<>();
        } catch (Exception e) {
            System.err.println("Corrupted state file, resetting: " + file);
            try (BufferedWriter w = Files.newBufferedWriter(file)) {
                w.write("{}");
            }
            return new HashMap<>();
        }
    }

    public void save(Map<Integer, StateEntry> states) throws IOException {
        Files.createDirectories(file.getParent());
        try (BufferedWriter w = Files.newBufferedWriter(file)) {
            w.write(gson.toJson(states, MAP_TYPE));
        }
    }
}
