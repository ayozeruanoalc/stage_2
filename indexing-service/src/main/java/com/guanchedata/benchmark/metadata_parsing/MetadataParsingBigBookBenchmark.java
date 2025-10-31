package com.guanchedata.benchmark.metadata_parsing;

import com.guanchedata.infrastructure.adapters.storer.metadata.MetadataParser;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value=1, jvmArgs = {"-Xmx4G"})
@State(Scope.Thread)
public class MetadataParsingBigBookBenchmark {

    //ruta datalake
    @Param({""})
    private String datalakePath;

    @Param({""})
    private String idBigBook;

    private MetadataParser metadataParser;

    @Setup(Level.Trial)
    public void setup() {
        metadataParser = new MetadataParser(datalakePath);
    }

    @Benchmark
    public void benchmarkParseMetadata() {
        metadataParser.parseMetadata(Integer.parseInt(idBigBook));
    }
}
