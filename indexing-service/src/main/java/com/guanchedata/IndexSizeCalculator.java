package com.guanchedata;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.sql.*;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class IndexSizeCalculator {
    Path metadataDbPath;
    String invertedIndexDbName;
    String collectionName;

    public IndexSizeCalculator(String metadataDbPath, String invertedIndexDbName, String collectionName) {
        this.metadataDbPath = Paths.get(metadataDbPath);
        this.invertedIndexDbName = invertedIndexDbName;
        this.collectionName = collectionName;
    }

    public String getTotalIndexSizeMB() {
        double sqliteSize = getSqliteDbSizeMB();
        double mongoSize = getMongoDbSizeMB();
        String result = String.format(Locale.US, "%.2f", sqliteSize + mongoSize);
        return result;
    }

    private double getSqliteDbSizeMB() {
        try {
            long bytes = Files.size(metadataDbPath);
            return bytes / (1024.0 * 1024.0);
        } catch (IOException e) {
            System.err.println("Error reading SQLite DB size: " + e.getMessage());
            return 0.0;
        }
    }

    private double getMongoDbSizeMB() {
        double sizeMB = 0.0;
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase(invertedIndexDbName);
            Document stats = database.runCommand(new Document("collStats", collectionName));
            long sizeBytes = stats.get("totalSize", Number.class).longValue();
            sizeMB = sizeBytes / (1024.0 * 1024.0);
        } catch (Exception e) {
            System.err.println("Error reading MongoDB size: " + e.getMessage());
        }
        return sizeMB;
    }

    public Set<Integer> getAlreadyIndexedBooksSet() {
        Set<Integer> ids = new HashSet<>();
        String url = "jdbc:sqlite:" + this.metadataDbPath;
        String query = "SELECT id FROM metadata";
        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                ids.add(rs.getInt("id"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ids;
    }
}