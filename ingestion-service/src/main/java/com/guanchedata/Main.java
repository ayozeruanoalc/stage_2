package com.guanchedata;

import com.guanchedata.application.usecases.ingestionservice.BookProviderController;
import com.guanchedata.infrastructure.adapters.*;
import com.guanchedata.infrastructure.ports.PathGenerator;
import io.javalin.Javalin;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args){
        PathGenerator pathGenerator = new DateTimePathGenerator(Paths.get(args[0]));
        GutenbergBookContentSeparator separator = new GutenbergBookContentSeparator();
        BookStorageDate storageDate = new BookStorageDate(pathGenerator, separator);
        BookDownloadLog bookDownloadLog = new BookDownloadLog(args[1]);
        BookProviderController controller = new BookProviderController(storageDate, bookDownloadLog);

        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";}).start(7001);

        app.post("/ingest/{book_id}",controller::ingestBook);
        app.get("/ingest/status/{book_id}", controller::status);
        app.get("/ingest/list", controller::listAllBooks);
    }
}
