package com.guanchedata.benchmark.indexingservice.databaseinsertion;

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

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value=1, jvmArgs = {"-Xmx4G"})
@State(Scope.Thread)
public class MongoDatabaseInsertionBenchmark {

    @Param({""})
    public String datalakePath;

    @Param({""})
    public String stopwordsPath;

    @Param({""})
    public String dbName;

    @Param({""})
    public String dbCollection;

    @Param({""})
    public String idBook;

    private MongoDBInvertedIndexStore mongoDBInvertedIndex;

    private Map<String, String> languageReferences;

    @Setup(Level.Trial)
    public void setup() {
        mongoDBInvertedIndex = new MongoDBInvertedIndexStore("mongodb://localhost:27017", datalakePath, stopwordsPath, dbName, dbCollection);
        languageReferences = new HashMap<>();
    }

    @Benchmark
    public void benchmarkBuildIndexForBooks() {
        languageReferences.put(idBook, "italian");
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
        }
    }


}
