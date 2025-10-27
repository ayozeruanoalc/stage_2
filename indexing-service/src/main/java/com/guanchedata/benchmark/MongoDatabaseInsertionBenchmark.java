package com.guanchedata.benchmark;

import com.guanchedata.infrastructure.adapters.provider.invertedindex.MongoDBInvertedIndexStore;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations=5)
@Measurement(iterations=10)
@Fork(1)
@State(Scope.Thread)
public class MongoDatabaseInsertionBenchmark {

    //ruta datalake
    @Param({""})
    private String datalakePath;

    //ruta stopwords
    @Param({""})
    private String stopwordsPath;

    //db name
    @Param({""})
    private String databaseName;

    //collection
    @Param({""})
    private String collectionName;

    private MongoDBInvertedIndexStore mongoDBInvertedIndex;

    private Map<String, String> languageReferences;

    @Setup(Level.Trial)
    public void setup() {
        mongoDBInvertedIndex = new MongoDBInvertedIndexStore(datalakePath, stopwordsPath, databaseName, collectionName);

        languageReferences = new HashMap<>();
        languageReferences.put("23", "en");
    }

    @Benchmark
    public void benchmarkBuildIndexForBooks() {
        mongoDBInvertedIndex.buildIndexForBooks(23, languageReferences);
    }
}
