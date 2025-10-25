package com.guanchedata.application.usecases.ingestionservice;

import com.google.gson.Gson;
import com.guanchedata.infrastructure.adapters.apiservices.BookStatusService;
import com.guanchedata.infrastructure.adapters.apiservices.IngestBookService;
import com.guanchedata.infrastructure.adapters.apiservices.ListBooksService;
import io.javalin.http.Context;
import java.util.Map;

public class BookProviderController {
    private final IngestBookService ingestBookService;
    private final ListBooksService listBooksService;
    private final BookStatusService bookStatusService;
    private static final Gson gson = new Gson();

    public BookProviderController(
            IngestBookService ingestBookService,
            ListBooksService listBooksService,
            BookStatusService bookStatusService) {
        this.ingestBookService = ingestBookService;
        this.listBooksService = listBooksService;
        this.bookStatusService = bookStatusService;
    }

    public void ingestBook(Context ctx) {
        int bookId = Integer.parseInt(ctx.pathParam("book_id"));
        Map<String, Object> result = ingestBookService.ingest(bookId);
        ctx.result(gson.toJson(result));
    }

    public void listAllBooks(Context ctx) {
        Map<String, Object> result = listBooksService.list();
        ctx.result(gson.toJson(result));
    }

    public void status(Context ctx) {
        int bookId = Integer.parseInt(ctx.pathParam("book_id"));
        Map<String, Object> result = bookStatusService.status(bookId);
        ctx.result(gson.toJson(result));
    }
}
