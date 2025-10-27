package com.guanchedata.infrastructure.adapters.apiservices;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.SQLException;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoCollection;

public class IndexEraser {
    Path metadataDbPath;
    String invertedIndexDbName;
    String collectionName;

    public IndexEraser(String metadataDbPath, String invertedIndexDbName, String collectionName){
        this.metadataDbPath = Paths.get(metadataDbPath);
        this.invertedIndexDbName = invertedIndexDbName;
        this.collectionName = collectionName;
    }

    public void erasePreviousIndex(){
        eraseMetadata();
        eraseInvertedIndex();
    }

    public void eraseMetadata(){
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + this.metadataDbPath)) {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("DELETE FROM metadata;");
        } catch (SQLException e) {
            System.err.println("Error erasing metadata: " + e.getMessage());
        }
    }

    public void eraseInvertedIndex(){
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase db = mongoClient.getDatabase(this.invertedIndexDbName);
            MongoCollection<?> collection = db.getCollection(this.collectionName);
            collection.deleteMany(new org.bson.Document());
        } catch (Exception e) {
            System.err.println("Error erasing inverted index (MongoDB): " + e.getMessage());
        }
    }
}
