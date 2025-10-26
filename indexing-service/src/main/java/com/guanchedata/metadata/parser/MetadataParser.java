package com.guanchedata.metadata.parser;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;

public class MetadataParser {
    private final String datalakePath;

    public MetadataParser(String datalakePath) {
        this.datalakePath = datalakePath;}

    public Map<String, String> parseMetadata(int bookId) {
        Path route = Paths.get(this.datalakePath);
        String targetFileName = bookId + "_header.txt";
        Map<String, String> metadata = new HashMap<>();

        try {
            Path found = Files.walk(route)
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().equals(targetFileName))
                    .findFirst()
                    .orElse(null);

            if (found != null) {
                System.out.println("[INDEX] Indexing book " + bookId + ".");
                try (BufferedReader reader = Files.newBufferedReader(found)) {
                    metadata = extractMetadata(reader);
                    if (!metadata.isEmpty()) {
                        System.out.println("[INDEX] Book " + bookId + " successfully indexed.\n");
                    } else {
                        System.out.println("[INDEX] No metadata found in book " + bookId + ".");
                    }
                }
            } else {
                System.out.println("[INDEX] No file found for book " + bookId + ".");
            }
        } catch (IOException e) {
            System.out.println("[INDEX] Error reading metadata for book " + bookId + ": " + e.getMessage());
        }
        return metadata;
    }

    private Map<String, String> extractMetadata(BufferedReader reader) throws IOException {
        Map<String, String> metadata = new HashMap<>();
        String line;
        Pattern pattern = Pattern.compile(
                "Title:\\s*(.+)|Author:\\s*(.+)|Language:\\s*(.+)|Release date:.*?(\\d{4})"
        );
        while ((line = reader.readLine()) != null) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.find()) {
                if (matcher.group(1) != null) metadata.put("Title", matcher.group(1));
                if (matcher.group(2) != null) metadata.put("Author", matcher.group(2));
                if (matcher.group(3) != null) metadata.put("Language", matcher.group(3));
                if (matcher.group(4) != null) metadata.put("Year", matcher.group(4));
            }
        }
        return metadata;
    }
}