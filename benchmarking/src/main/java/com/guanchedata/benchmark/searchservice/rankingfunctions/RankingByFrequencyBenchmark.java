package com.guanchedata.benchmark.searchservice.rankingfunctions;

import com.guanchedata.util.ResultsSorterByFreq;
import org.openjdk.jmh.annotations.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value=1, jvmArgs = {"-Xmx4G"})
@State(Scope.Thread)
public class RankingByFrequencyBenchmark {

    @Param({"10", "100", "1000"})
    private int numberOfBooks;

    private List<Map<String, Object>> books;

    @Setup(Level.Invocation)
    public void setup() {
        books = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < numberOfBooks; i++) {
            Map<String, Object> book = new HashMap<>();
            book.put("id", i);
            book.put("frequency", random.nextInt(301));
            book.put("author", "author" + random.nextInt(9999));
            book.put("title", "title" + random.nextInt(9999));
            book.put("language", "language" + random.nextInt(9999));
            book.put("year", random.nextInt(1940, 2025));
            books.add(book);
        }
    }

    @Benchmark
    public void benchmarkSortByFrequency() {
        ResultsSorterByFreq sorter = new ResultsSorterByFreq();
        sorter.sort(books, null);
    }
}
