package com.guanchedata.application.usecases.ingestionservice;

import com.google.gson.Gson;
import com.guanchedata.infrastructure.adapters.*;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class BookProviderController {
    private final BookStorageDate storageDate;
    private final BookDownloadLog bookDownloadLog;
    private static final Gson gson = new Gson();
    private static final Logger log = LoggerFactory.getLogger(BookProviderController.class);

    public BookProviderController(BookStorageDate storageDate, BookDownloadLog bookDownloadLog) {
        this.storageDate = storageDate;
        this.bookDownloadLog = bookDownloadLog;
    }

    public void status(Context ctx) throws IOException {
        int bookId = Integer.parseInt(ctx.pathParam("book_id"));
        log.info("status() - Start execution for bookId={}", bookId);
        boolean isBookAvailable = bookDownloadLog.isDownloaded(bookId);


        if (isBookAvailable){
            log.info("status() - Book {} is available in datalake", bookId);
            Map<String, Object> response = Map.of(
                    "book_id", bookId,
                    "status", "available"
            );

            ctx.result(gson.toJson(response));
        } else {
            log.warn("status() - Book {} is not available in datalake", bookId);

            Map<String, Object> response = Map.of(
                    "book_id", bookId,
                    "status", "not_available"
            );

            ctx.result(gson.toJson(response));
        }
        log.info("status() - Finished execution for bookId={}", bookId);
    }

    public void listAllBooks(Context ctx) throws IOException {
        log.info("listAllBooks() - List of books in the datalake");
        List<Integer> downloadedBooks = bookDownloadLog.getAllDownloaded();
        Map<String, Object> response = Map.of(
                "count", downloadedBooks.size(),
                "books", downloadedBooks
        );
        ctx.result(gson.toJson(response));
        log.info("listAllBooks() - Finished execution for the list of books");
    }

    public void ingestBook(Context ctx) {
        int bookId = Integer.parseInt(ctx.pathParam("book_id"));
        log.info("ingestBook() - Start processing bookId={}", bookId);

        try {
            if (bookDownloadLog.isDownloaded(bookId)) {
                log.warn("ingestBook() - Book {} is already downloaded, skipping ingestion", bookId);
                Map<String, Object> responseAPI = Map.of(
                        "book_id", bookId,
                        "status", "already_downloaded",
                        "message", "Book already exists in datalake"
                );
                ctx.result(gson.toJson(responseAPI));
                return;
            }

            GutenbergConnection connection = new GutenbergConnection();
            GutenbergFetch fetch = new GutenbergFetch();
            String response = fetch.fetchBook(connection.createConnection(bookId));
            Path savedPath = storageDate.save(bookId, response);
            bookDownloadLog.registerDownload(bookId);

            log.info("ingestBook() - Book {} downloaded and saved at {}", bookId, savedPath);

            Map<String, Object> responseAPI = Map.of(
                    "book_id", bookId,
                    "status", "downloaded",
                    "path", savedPath.toString().replace("\\", "/")
            );

            ctx.result(gson.toJson(responseAPI));

        } catch (Exception e) {
            log.error("ingestBook() - Error processing bookId {}: {}", bookId, e.getMessage(), e);

            Map<String, Object> responseError = Map.of(
                    "book_id", bookId,
                    "status", "error",
                    "message", e.getMessage()
            );

            ctx.result(gson.toJson(responseError));
        } finally {
            log.info("ingestBook() - Finished processing bookId={}", bookId);
        }
    }
}
