package com.guanchedata.core;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ArgsParser {
    private final Path statePath;
    private final List<Integer> bookIds;

    public ArgsParser(String[] args) throws Exception {
        if (args == null || args.length < 2) {
            System.err.println("usage: java com.guanchedata.Main <state_file_path> <bookId> [<bookId> ...]");
            System.exit(1);
        }

        this.statePath = prepareStateFile(args[0]);
        this.bookIds = parseIds(args);
    }

    public Path getStatePath() {
        return statePath;
    }

    public List<Integer> getBookIds() {
        return bookIds;
    }

    private static Path prepareStateFile(String path) throws Exception {
        Path statePath = Path.of(path);
        Files.createDirectories(statePath.getParent() == null ? Path.of(".") : statePath.getParent());
        return statePath;
    }

    private static List<Integer> parseIds(String[] args) {
        List<Integer> out = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            out.add(Integer.parseInt(args[i]));
        }
        return out;
    }
}
