package com.guanchedata.infrastructure.adapters.provider.index;

import com.guanchedata.infrastructure.ports.InvertedIndexProvider;
import com.mongodb.client.*;
import org.bson.Document;

public class MongoDBConnector implements InvertedIndexProvider {
    private static MongoClient mongoClient;
    private static MongoDatabase database;
    private static MongoCollection<Document> collection;

    public MongoDBConnector(String uri, String dbName, String collectionName) {
        MongoClient client = MongoClients.create(uri);
        MongoDatabase database = client.getDatabase(dbName);
        this.collection = database.getCollection(collectionName);
    }

    @Override
    public Document findWord(String word) {
        return collection.find(new Document("word", word)).first();
    }
}
