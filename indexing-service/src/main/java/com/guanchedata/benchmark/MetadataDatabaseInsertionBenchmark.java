package com.guanchedata.benchmark;

import com.guanchedata.metadata.storage.sqlite.MetadataSQLiteDB;
import com.guanchedata.metadata.parser.MetadataParser;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations=5)
@Measurement(iterations=10)
@Fork(1)
@State(Scope.Thread)
public class MetadataDatabaseInsertionBenchmark {

    //ruta datalake
    @Param({""})
    private String datalakePath;

    //ruta metadata.db
    @Param({""})
    private String dbPath;

    private MetadataSQLiteDB metadataDB;

    @Setup(Level.Trial)
    public void setup() {
        MetadataParser parser = new MetadataParser(datalakePath);
        metadataDB = new MetadataSQLiteDB(parser, dbPath);
    }

    @Benchmark
    public void benchmarkSaveMetadata() {
        metadataDB.saveMetadata(23);
    }
}
