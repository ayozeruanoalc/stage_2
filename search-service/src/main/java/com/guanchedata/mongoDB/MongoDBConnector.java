package com.guanchedata.mongoDB;

import com.mongodb.client.*;
import org.bson.Document;

public class MongoDBConnector {
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;

    public MongoDBConnector(String uri, String dbName, String collectionName) {
        MongoClient client = MongoClients.create(uri);
        MongoDatabase database = client.getDatabase(dbName);
        this.collection = database.getCollection(collectionName);
    }

    public Document findWord(String word) {
        return collection.find(new Document("word", word)).first();
    }
}
