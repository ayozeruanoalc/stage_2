package com.guanchedata.infrastructure.adapters.apiservices;

import com.guanchedata.infrastructure.adapters.storer.invertedindex.MongoDBInvertedIndexStore;
import com.guanchedata.infrastructure.adapters.storer.metadata.MetadataParser;
import com.guanchedata.infrastructure.adapters.storer.metadata.MetadataSQLiteDB;

import java.util.Map;

public class BookIndexer {

    String datalakePath;
    String dbPath;
    String stopwordsPath;
    String databaseName;
    String collectionName;

    public BookIndexer(String datalakePath, String dbPath, String stopwordsPath, String databaseName, String collectionName) {
        this.datalakePath = datalakePath;
        this.dbPath = dbPath;
        this.stopwordsPath = stopwordsPath;
        this.databaseName = databaseName;
        this.collectionName = collectionName;

    }

    public void execute(int bookId) {

        MetadataParser parser = new MetadataParser(this.datalakePath);
        MetadataSQLiteDB metadataDB = new MetadataSQLiteDB(parser, this.dbPath);
        MongoDBInvertedIndexStore mongoDB = new MongoDBInvertedIndexStore(this.datalakePath, this.stopwordsPath, this.databaseName, this.collectionName);

        Map<String, String> languages = metadataDB.saveMetadata(bookId);
        mongoDB.buildIndexForBooks(bookId, languages);

    }
}
