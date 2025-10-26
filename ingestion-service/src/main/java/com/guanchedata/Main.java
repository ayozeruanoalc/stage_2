package com.guanchedata;

import com.guanchedata.application.usecases.ingestionservice.BookProviderController;
import com.guanchedata.infrastructure.adapters.apiservices.BookStatusService;
import com.guanchedata.infrastructure.adapters.apiservices.IngestBookService;
import com.guanchedata.infrastructure.adapters.apiservices.ListBooksService;
import com.guanchedata.infrastructure.adapters.bookprovider.BookDownloadLog;
import com.guanchedata.infrastructure.adapters.bookprovider.BookStorageDate;
import com.guanchedata.infrastructure.adapters.bookprovider.GutenbergBookContentSeparator;
import com.guanchedata.infrastructure.ports.PathGenerator;
import com.guanchedata.util.DateTimePathGenerator;
import io.javalin.Javalin;

public class Main {
    public static void main(String[] args) {
        PathGenerator pathGenerator = new DateTimePathGenerator(args[0]);
        GutenbergBookContentSeparator separator = new GutenbergBookContentSeparator();
        BookStorageDate storageDate = new BookStorageDate(pathGenerator, separator);
        BookDownloadLog bookDownloadLog = new BookDownloadLog(args[1]);

        IngestBookService ingestBookService = new IngestBookService(storageDate, bookDownloadLog);
        ListBooksService listBooksService = new ListBooksService(bookDownloadLog);
        BookStatusService bookStatusService = new BookStatusService(bookDownloadLog);

        BookProviderController controller = new BookProviderController(
                ingestBookService,
                listBooksService,
                bookStatusService
        );

        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
        }).start(7001);

        app.post("/ingest/{book_id}", controller::ingestBook);
        app.get("/ingest/status/{book_id}", controller::status);
        app.get("/ingest/list", controller::listAllBooks);
    }
}
