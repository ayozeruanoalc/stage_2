package com.guanchedata.benchmark;

import com.guanchedata.util.ControlRunner;
import org.openjdk.jmh.annotations.*;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value=1, jvmArgs = {"-Xmx4G"})
@State(Scope.Thread)
public class ControlRunnerBenchmark {

    @Param({"10", "25", "50"})
    private int numberOfBooks;

    private ControlRunner controlRunner;
    private Path stateFile;
    private String[] args;
    private PrintStream originalOut;
    private PrintStream originalErr;

    @Setup(Level.Trial)
    public void setup() throws Exception {
        stateFile = Files.createTempFile("bench", ".json");
        args = new String[numberOfBooks + 1];
        args[0] = stateFile.toString();
        for (int i = 0; i < numberOfBooks; i++) {
            args[i + 1] = String.valueOf(i + 1);
        }
        originalOut = System.out;
        originalErr = System.err;
    }

    @Setup(Level.Invocation)
    public void cleanState() throws Exception {
        Files.deleteIfExists(stateFile);
        Files.createFile(stateFile);
        controlRunner = new ControlRunner();

        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        System.setErr(new PrintStream(new ByteArrayOutputStream()));
    }

    @Benchmark
    public void benchmarkRun() throws Exception {
        controlRunner.run(args);
    }

    @TearDown(Level.Invocation)
    public void restoreOutput() {
        System.setOut(originalOut);
        System.setErr(originalErr);
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        Files.deleteIfExists(stateFile);
    }
}