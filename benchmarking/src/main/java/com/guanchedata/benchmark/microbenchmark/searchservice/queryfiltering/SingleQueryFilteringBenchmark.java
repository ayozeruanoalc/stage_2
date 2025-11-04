package com.guanchedata.benchmark.microbenchmark.searchservice.queryfiltering;

import com.guanchedata.infrastructure.adapters.apiservices.SearchService;
import com.guanchedata.infrastructure.adapters.provider.index.MongoDBConnector;
import com.guanchedata.infrastructure.adapters.provider.metadata.SQLiteConnector;
import com.guanchedata.infrastructure.ports.InvertedIndexProvider;
import com.guanchedata.infrastructure.ports.MetadataProvider;
import org.openjdk.jmh.annotations.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value=1, jvmArgs = {"-Xmx4G"})
@State(Scope.Thread)

public class SingleQueryFilteringBenchmark {

    @Param({""})
    private String metadataPath;

    @Param({""})
    private String mongoDbName;

    @Param({""})
    private String mongoDbCollection;

    @Param({"mongodb://localhost:27017/"})
    private String mongoDbUri;

    @Param({"horse"})
    private String word;

    @Param({"english"})
    private String language;

    private Map<String, Object> filters = new HashMap<>();

    private SearchService searchService;
    private MetadataProvider metadataProvider;

    @Setup(Level.Trial)
    public void setup() {
        filters.put("language", language);
        metadataProvider = new SQLiteConnector(metadataPath);
        InvertedIndexProvider invertedIndexConnector = new MongoDBConnector(mongoDbUri, mongoDbName, mongoDbCollection);
        searchService = new SearchService(invertedIndexConnector, metadataProvider, null);
    }

    @Benchmark
    public void singleQueryFilteringBenchmark() {
        List<Integer> docsids = searchService.getBooksContainsWord(word);
        List<Map<String, Object>> results = metadataProvider.findMetadata(docsids, filters);
    }
}
