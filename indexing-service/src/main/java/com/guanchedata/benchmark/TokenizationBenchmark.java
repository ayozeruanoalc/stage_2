package com.guanchedata.benchmark;

import com.guanchedata.inverted_index.mongodb.BookIndexProcessor;
import com.guanchedata.inverted_index.stopwords.StopwordsLoader;
import org.openjdk.jmh.annotations.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Warmup(iterations=5)
@Measurement(iterations=10)
@Fork(1)
@State(Scope.Thread)
public class TokenizationBenchmark {

    //ruta de un libro solo
    @Param({""})
    private String sampleFilePath;

    // ruta stopwords
    @Param({""})
    private String stopwordsJsonPath;

    private BookIndexProcessor processor;
    private Path file;
    private Set<String> stopwords;

    @Setup(Level.Trial)
    public void setup() {
        processor = new BookIndexProcessor();
        file = Paths.get(sampleFilePath);
        stopwords = StopwordsLoader.loadStopwords(stopwordsJsonPath, new java.util.HashMap<>(), "en");
    }

    @Benchmark
    public Map<String, java.util.List<Integer>> benchmarkExtractWordPositions() {
        return processor.extractWordPositions(file, stopwords);
    }
}
