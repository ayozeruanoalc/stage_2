package com.guanchedata;

import com.guanchedata.mongoDB.MongoDBConnector;
import com.guanchedata.sqlite.SQLiteConnector;

import io.javalin.Javalin;
import io.javalin.json.JavalinGson;


public class Main {
    public static void main(String[] args) {
        SQLiteConnector sqliteConnector = new SQLiteConnector(args[0]);
        MongoDBConnector mongoDBConnector = new MongoDBConnector(args[1], args[2], args[3]);

        SearchService searchService = new SearchService(mongoDBConnector, sqliteConnector);
        SearchController searchController = new SearchController(searchService);

        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
            config.jsonMapper(new JavalinGson());
        }).start(7003);

        app.get("/search", searchController::getSearch);

        System.out.println("API running in port 7003");
    }
}
