package com.guanchedata.infrastructure.adapters.statestorer;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.guanchedata.domain.StateEntry;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StateFileManager {
    private final Path file;
    private final Gson gson;
    private static final Type MAP_TYPE = new TypeToken<Map<Integer, StateEntry>>(){}.getType();

    public StateFileManager(Path file, Gson gson) throws IOException {
        this.file = file;
        this.gson = gson;
        Path dir = file.getParent() == null ? Path.of(".") : file.getParent();
        Files.createDirectories(dir);
        if (!Files.exists(file)) {
            save(new HashMap<>());
        }
    }

    public Map<Integer, StateEntry> load() throws IOException {
        if (!Files.exists(file) || Files.size(file) == 0L) {
            return new HashMap<>();
        }
        try (Reader r = Files.newBufferedReader(file)) {
            Map<Integer, StateEntry> data = gson.fromJson(r, MAP_TYPE);
            return data != null ? data : new HashMap<>();
        }
    }

    public void save(Map<Integer, StateEntry> states) throws IOException {
        Map<Integer, StateEntry> toWrite = states != null ? states : Collections.emptyMap();
        try (Writer w = Files.newBufferedWriter(file)) {
            gson.toJson(toWrite, MAP_TYPE, w);
        }
    }
}
