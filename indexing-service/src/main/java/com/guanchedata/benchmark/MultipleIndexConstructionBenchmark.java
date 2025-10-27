package com.guanchedata.benchmark;

import com.guanchedata.infrastructure.adapters.apiservices.BookIndexer;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(1)
@State(Scope.Thread)
public class MultipleIndexConstructionBenchmark {

    //ruta datalake
    @Param({""})
    private String arg0;

    //ruta metadata.db
    @Param({""})
    private String arg1;

    //ruta stopwords
    @Param({""})
    private String arg2;

    //nombre db
    @Param({""})
    private String arg3;

    //collection
    @Param({""})
    private String arg4;

    //id book
    @Param({""})
    private String arg5;

    private BookIndexer bookIndexer;

    @Setup(Level.Trial)
    public void setup() {
        bookIndexer = new BookIndexer(arg0, arg1, arg2, arg3, arg4);
    }

    @Benchmark
    public void benchmarkExecute() {
        int id = Integer.parseInt(arg5);
        for (int i=1; i<=id; i++) {
            bookIndexer.execute(i);
        }
    }
}
