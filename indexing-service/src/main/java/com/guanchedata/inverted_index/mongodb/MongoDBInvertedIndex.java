package com.guanchedata.inverted_index.mongodb;

import com.guanchedata.inverted_index.InvertedIndex;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.*;
import org.bson.Document;

import java.util.List;
import java.util.Map;
import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public class MongoDBInvertedIndex implements InvertedIndex {

    private String datalakePath;
    private String stopwordsPath;
    private String databaseName;
    private String collectionName;
    private MongoClient client;
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private Map<String, Set<String>> stopwordsCache = new HashMap<>();

    public MongoDBInvertedIndex(String datalakePath, String stopwordsPath, String databaseName, String collectionName) {
        this.datalakePath = datalakePath;
        this.stopwordsPath = stopwordsPath;
        this.databaseName = databaseName;
        this.collectionName = collectionName;
        this.client = MongoClients.create("mongodb://localhost:27017");
        this.database = client.getDatabase(this.databaseName);
        this.collection = database.getCollection(this.collectionName);
        this.collection.createIndex(new Document("word", 1), new IndexOptions().unique(true));
    }

    public  void saveIndexForBook(int bookId, Map<String, List<Integer>> positionMap) {
        List<WriteModel<Document>> operation = new ArrayList<>();
        String bookIdStr = String.valueOf(bookId);

        for (Map.Entry<String, List<Integer>> entry : positionMap.entrySet()) {
            String word = entry.getKey();
            List<Integer> positions = entry.getValue();
            Document data = new Document("frecuency", positions.size())
                    .append("position", positions);

            operation.add(new UpdateOneModel<>(
                    Filters.eq("word", word),
                    Updates.set("documents." + bookIdStr, data),
                    new UpdateOptions().upsert(true)
            ));

            if (operation.size() >= 5000){
                collection.bulkWrite(operation, new BulkWriteOptions().ordered(false));
                operation.clear();
            }

        }
        if (!operation.isEmpty()) {
            collection.bulkWrite(operation, new BulkWriteOptions().ordered(false));
        }

    }

    public void buildIndexForBooks(Integer bookId, Map<String, String> languageReferences) {
        Path route = Paths.get(this.datalakePath);
        BookIndexProcessor processor = new BookIndexProcessor();

        try (Stream<Path> files = Files.walk(route)) {
            files.filter(path -> path.getFileName().toString().matches("\\d+_.*body\\.txt"))
                    .forEach(f -> processor.processBookFile(f, bookId, languageReferences, stopwordsPath, stopwordsCache, this));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }




}
