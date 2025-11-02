package com.guanchedata;
import com.guanchedata.application.usecases.indexingservice.IndexingController;
import com.guanchedata.infrastructure.adapters.apiservices.BookIndexerService;
import com.guanchedata.util.DatalakeBookIdExtractor;
import com.guanchedata.infrastructure.adapters.apiservices.IndexEraserService;
import com.guanchedata.infrastructure.adapters.apiservices.IndexSizeCalculatorService;
import io.javalin.Javalin;

public class Main {

    public static void main(String[] args) {
        IndexingController indexingController = new IndexingController(new BookIndexerService(args[0], args[1], args[2], args[3], args[4], args[5]), new IndexEraserService(args[1],args[3],args[4]), new DatalakeBookIdExtractor(args[0]), new IndexSizeCalculatorService(args[1],args[3],args[4]));
        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";}).start(7002);

        app.post("/index/update/{book_id}", indexingController::indexBook);
        app.post("/index/rebuild", indexingController::rebuildIndex);
        app.get("/index/status", indexingController::retrieveIndexingStatus);
    }
}
