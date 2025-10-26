package com.guanchedata.metadata.storage.sqlite;

import com.guanchedata.metadata.parser.MetadataParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

public class MetadataSQLiteDB  {
    private final MetadataParser metadataParser;
    private final Path dbPath;

    public MetadataSQLiteDB(MetadataParser metadataParser, String dbPathStr) {
        this.metadataParser = metadataParser;
        this.dbPath = Paths.get(dbPathStr);
    }

    public Map<String, String> saveMetadata(int bookId) {
        Map<String, String> metadata = metadataParser.parseMetadata(bookId);
        Map<String, Map<String, String>> allMetadata = new HashMap<>();
        if (metadata != null && !metadata.isEmpty()) {
            allMetadata.put(String.valueOf(bookId), metadata);
            insertMetadata(bookId,
                    metadata.getOrDefault("Title", null),
                    metadata.getOrDefault("Author", null),
                    metadata.getOrDefault("Language", null),
                    metadata.getOrDefault("Year", null));
            return extractLanguage(allMetadata);
        }
        return new HashMap<>();
    }

    private String insertMetadata(int id, String title, String author, String language, String year) {
        try {
            Path parentDir = this.dbPath.getParent();
            if (!Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }
            if (id == 0 || title == null) return "error";
            try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + this.dbPath)) {
                conn.createStatement().executeUpdate(
                        "CREATE TABLE IF NOT EXISTS metadata (" +
                                "id INTEGER PRIMARY KEY, title TEXT NOT NULL, author TEXT, language TEXT, year TEXT)"
                );
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO metadata (id, title, author, language, year) VALUES (?, ?, ?, ?, ?)")) {
                    ps.setInt(1, id);
                    ps.setString(2, title);
                    ps.setString(3, author);
                    ps.setString(4, language);
                    ps.setString(5, year);
                    ps.executeUpdate();
                }
            }
            return "ok";
        } catch (SQLException e) {
            System.out.println("Error saving metadata: " + e.getMessage());
            return "error";
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Map<String, String> extractLanguage(Map<String, Map<String, String>> allMetadata) {
        Map<String, String> languages = new HashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : allMetadata.entrySet()) {
            String idx = entry.getKey();
            Map<String, String> metadata = entry.getValue();
            String lang = metadata.get("Language");
            languages.put(idx, lang == null ? null : lang.toLowerCase());
        }
        return languages;
    }
}