package com.guanchedata;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;

public class DatalakeBookIdExtractor {
    Path datalakePath;

    public DatalakeBookIdExtractor(String datalakePath) {
        this.datalakePath = Paths.get(datalakePath);
    }

    public HashSet<Integer> generateDownloadedBooksSet() {
        HashSet<Integer> bookIds = new HashSet<>();
        try {
            Files.walk(this.datalakePath)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith("_header.txt"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        try {
                            String idStr = fileName.substring(0, fileName.indexOf("_header.txt"));
                            int id = Integer.parseInt(idStr);
                            bookIds.add(id);
                        } catch (Exception ignored) {}
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bookIds;
    }
}
