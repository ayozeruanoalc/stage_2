package com.guanchedata.application.usecases.indexingservice;

import com.google.gson.Gson;
import com.guanchedata.infrastructure.adapters.apiservices.*;
import com.guanchedata.infrastructure.ports.BookIdExtractor;
import com.guanchedata.infrastructure.ports.BookIndexer;
import com.guanchedata.infrastructure.ports.IndexEraser;
import com.guanchedata.infrastructure.ports.IndexSizeCalculator;
import io.javalin.http.Context;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;

public class IndexingController {
    private final BookIndexer bookIndexer;
    private final IndexEraser indexEraserService;
    private final BookIdExtractor datalakeBookIdExtractor;
    private final IndexSizeCalculator indexSizeCalculatorService;
    private Set<Integer> indexedBooksSet;
    private Instant last_update;
    private final Gson gson;

    public IndexingController(BookIndexer bookIndexer, IndexEraser indexEraserService, BookIdExtractor datalakeBookIdExtractor,
                              IndexSizeCalculator indexSizeCalculatorService) {
        this.bookIndexer = bookIndexer;
        this.indexEraserService = indexEraserService;
        this.datalakeBookIdExtractor = datalakeBookIdExtractor;
        this.indexSizeCalculatorService = indexSizeCalculatorService;
        this.indexedBooksSet = this.indexSizeCalculatorService.getAlreadyIndexedBooksSet();
        this.last_update = Instant.EPOCH;
        this.gson = new Gson();
    }

    public void indexBook(Context ctx){
        int bookId = Integer.parseInt(ctx.pathParam("book_id"));
        this.bookIndexer.index(bookId);
        this.indexedBooksSet.add(bookId);
        this.last_update = Instant.now();
        Map<String, Object> response = Map.of( "index", "updated","book_id", bookId);
        ctx.result(gson.toJson(response));
    }

    public void rebuildIndex(Context ctx){
        this.indexEraserService.erasePreviousIndex();
        this.indexedBooksSet = this.datalakeBookIdExtractor.generateDownloadedBooksSet();
        Instant start = Instant.now();
        for (int bookId : this.indexedBooksSet){
            this.bookIndexer.index(bookId);
        }
        Instant end = Instant.now();
        double elapsedSeconds = Duration.between(start, end).toMillis() / 1000.0;
        String elapsedTime = String.format("%.1fs", elapsedSeconds);
        this.last_update = end;
        Map<String, Object> response = Map.of("books_processed", this.indexedBooksSet.size(), "elapsed_time", elapsedTime);
        ctx.result(gson.toJson(response));
    }

    public void retrieveIndexingStatus(Context ctx){
            Map<String, Object> response = Map.of(
                    "books_indexed", this.indexedBooksSet.size(),
                    "last_update", this.last_update.toString(),
                    "index_size_MB", this.indexSizeCalculatorService.getTotalIndexSizeMB()
            );
            ctx.result(gson.toJson(response));
    }
}
