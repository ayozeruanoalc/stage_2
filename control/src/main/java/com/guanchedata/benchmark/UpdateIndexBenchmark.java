package com.guanchedata.benchmark;

import com.guanchedata.infrastructure.adapters.apiintegrator.PipelineCoordinator;
import com.guanchedata.infrastructure.adapters.apiintegrator.RestConnector;
import org.openjdk.jmh.annotations.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value=1, jvmArgs = {"-Xmx4G"})
@State(Scope.Thread)
public class UpdateIndexBenchmark {

    @Param({"10", "25", "50"})
    private int numberOfBooks;

    private PipelineCoordinator coordinator;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @Setup(Level.Trial)
    public void setup() {
        coordinator = new PipelineCoordinator(
                new RestConnector(),
                "http://localhost:7001",
                "http://localhost:7002"
        );
        originalOut = System.out;
        originalErr = System.err;
    }

    @Setup(Level.Invocation)
    public void suppressOutput() {
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
    }

    @Benchmark
    public void benchmarkIndexing() {
        for (int i = 1; i <= numberOfBooks; i++) {
            try {
                coordinator.updateIndex(i);
            } catch (Exception e) {
            }
        }
    }

    @TearDown(Level.Invocation)
    public void restoreOutput() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }
}