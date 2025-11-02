package com.guanchedata;

import com.guanchedata.application.usecases.searchservice.SearchController;
import com.guanchedata.infrastructure.adapters.apiservices.SearchService;
import com.guanchedata.infrastructure.adapters.provider.index.MongoDBConnector;
import com.guanchedata.infrastructure.adapters.provider.metadata.SQLiteConnector;

import com.guanchedata.infrastructure.ports.BookSearchProvider;
import com.guanchedata.infrastructure.ports.MetadataProvider;
import com.guanchedata.infrastructure.ports.ResultsSorter;
import com.guanchedata.infrastructure.ports.InvertedIndexProvider;
import com.guanchedata.util.ResultsSorterByFreq;
import com.guanchedata.util.ResultsSorterById;
import io.javalin.Javalin;
import io.javalin.json.JavalinGson;


public class Main {
    public static void main(String[] args) {
        MetadataProvider metadataConnector = new SQLiteConnector(args[0]);
        InvertedIndexProvider invertedIndexConnector = new MongoDBConnector(args[1], args[2], args[3]);


        ResultsSorter resultsSorter = null;
        if(args[4].equalsIgnoreCase("id")){
            resultsSorter = new ResultsSorterById();
        } else if(args[4].equalsIgnoreCase("frequency")){
            resultsSorter = new ResultsSorterByFreq();
        } else {
            System.err.println("Sorter method must be either 'id' or 'frequency'");
        }

        BookSearchProvider searchService = new SearchService(invertedIndexConnector, metadataConnector,  resultsSorter);
        SearchController searchController = new SearchController(searchService);

        Javalin app = Javalin.create(config -> {
            config.http.defaultContentType = "application/json";
            config.jsonMapper(new JavalinGson());
        }).start(7003);

        app.get("/search", searchController::getSearch);

        System.out.println("API running in port 7003");
    }
}
