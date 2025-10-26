package com.guanchedata.benchmark;

import com.guanchedata.metadata.parser.MetadataParser;
import org.openjdk.jmh.annotations.*;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations=5)
@Measurement(iterations=10)
@Fork(1)
@State(Scope.Thread)
public class MetadataParsingBenchmark {

    //ruta datalake
    @Param({""})
    private String datalakePath;

    private MetadataParser metadataParser;

    @Setup(Level.Trial)
    public void setup() {
        metadataParser = new MetadataParser(datalakePath);
    }

    @Benchmark
    public Map<String, String> benchmarkParseMetadata() {
        return metadataParser.parseMetadata(23);
    }
}
