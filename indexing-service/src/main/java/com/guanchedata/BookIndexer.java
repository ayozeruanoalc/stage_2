package com.guanchedata;

import com.guanchedata.inverted_index.mongodb.MongoDBInvertedIndex;
import com.guanchedata.metadata.parser.MetadataParser;
import com.guanchedata.metadata.storage.sqlite.MetadataSQLiteDB;

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
        MongoDBInvertedIndex mongoDB = new MongoDBInvertedIndex(this.datalakePath, this.stopwordsPath, this.databaseName, this.collectionName);

        Map<String, String> languages = metadataDB.saveMetadata(bookId);
        //System.out.println(languages);
        mongoDB.buildIndexForBooks(bookId, languages);

    }
}
