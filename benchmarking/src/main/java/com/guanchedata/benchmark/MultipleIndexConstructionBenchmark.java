package com.guanchedata.benchmark;

import com.guanchedata.infrastructure.adapters.apiservices.BookIndexer;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.openjdk.jmh.annotations.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value=1, jvmArgs = {"-Xmx4G"})
@State(Scope.Thread)
public class MultipleIndexConstructionBenchmark {

    //ruta datalake
    @Param({})
    private String datalakePath;

    //ruta metadata.db
    @Param({})
    private String metadataPath;

    //ruta stopwords
    @Param({})
    private String stopwordsPath;

    //nombre db
    @Param({})
    private String dbName;

    //collection
    @Param({})
    private String dbCollection;

    @Param({})
    private String idBook;



    private BookIndexer bookIndexer;

    @Setup(Level.Trial)
    public void setup() {
        bookIndexer = new BookIndexer(datalakePath, metadataPath, stopwordsPath, dbName, dbCollection, "mongodb://localhost:27017");
    }

    @Benchmark
    public void benchmarkExecute() {
        try {
            Files.deleteIfExists(Paths.get(metadataPath));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase(dbName);
            MongoCollection<Document> collection = database.getCollection(dbCollection);
            collection.deleteMany(new Document());
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        bookIndexer.execute(Integer.parseInt(idBook));
    }
}
