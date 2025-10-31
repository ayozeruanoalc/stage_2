package com.guanchedata.benchmark.database_insertion;

import com.guanchedata.infrastructure.adapters.storer.invertedindex.MongoDBInvertedIndexStore;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value=1, jvmArgs = {"-Xmx4G"})
@State(Scope.Thread)
public class MongoDatabaseInsertionBenchmark {

    @Param({""})
    private String datalakePath;

    @Param({""})
    private String stopwordsPath;

    @Param({""})
    private String dbName;

    @Param({""})
    private String dbCollection;

    @Param({""})
    private String idBook;

    private MongoDBInvertedIndexStore mongoDBInvertedIndex;

    private Map<String, String> languageReferences;

    @Setup(Level.Trial)
    public void setup() {
        mongoDBInvertedIndex = new MongoDBInvertedIndexStore(datalakePath, stopwordsPath, dbName, dbCollection);
        languageReferences = new HashMap<>();
    }

    @Benchmark
    public void benchmarkBuildIndexForBooks() {
        languageReferences.put(idBook, "english");
        mongoDBInvertedIndex.buildIndexForBooks(Integer.parseInt(idBook), languageReferences);
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = database.getCollection(dbCollection);
            String keyToRemove = "documents." + idBook;
            Document filter = new Document(keyToRemove, new Document("$exists", true));
            Document unsetUpdate = new Document("$unset", new Document(keyToRemove, ""));
            collection.updateMany(filter, unsetUpdate);
            Document emptyDocumentsFilter = new Document("documents", new Document("$eq", new Document()));
            collection.deleteMany(emptyDocumentsFilter);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


}
