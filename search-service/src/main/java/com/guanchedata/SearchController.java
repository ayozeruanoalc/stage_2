package com.guanchedata;

import io.javalin.http.Context;
import java.util.*;

public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    public void getSearch(Context ctx) {
        String query = ctx.queryParam("q");
        String author = ctx.queryParam("author");
        String language = ctx.queryParam("language");
        String yearParam = ctx.queryParam("year");

        if (query == null || query.isEmpty()) {
            ctx.status(400).json(Map.of("error", "Missing required parameter 'q'"));
            return;
        }

        Map<String, Object> filters = new HashMap<>();
        if (author != null && !author.isEmpty()) filters.put("author", author);
        if (language != null && !language.isEmpty()) filters.put("language", language);
        if (yearParam != null && !yearParam.isEmpty()) {
            try {
                filters.put("year", Integer.parseInt(yearParam));
            } catch (NumberFormatException e) {
                ctx.status(400).json(Map.of("error", "Invalid year format"));
                return;
            }
        }

        List<Map<String, Object>> results = new  ArrayList<>();
        if (query.contains(",") && Arrays.asList(query.split(",")).size() > 1) {
            List<String> queries = Arrays.asList(query.split(","));
            List<List<Map<String, Object>>> resultsList = new ArrayList<>();
            for (int i = 0; i < queries.size(); i++) {
                resultsList.add(searchService.search(queries.get(i), filters));
            }

            Set<String> commonTitles = new HashSet<>();
            resultsList.get(0).forEach(map -> commonTitles.add(map.get("title").toString()));

            for (int i = 1; i < resultsList.size(); i++) {
                Set<String> titles = new HashSet<>();
                for (Map<String, Object> map : resultsList.get(i)) {
                    titles.add(map.get("title").toString());
                }
                commonTitles.retainAll(titles);
            }

            results = resultsList.get(0).stream()
                    .filter(map -> commonTitles.contains(map.get("title").toString()))
                    .toList();
        } else {
            results = searchService.search(query, filters);
        }

        Map<String, Object> response = new LinkedHashMap<>();
        System.out.println(results);
        response.put("query", query);
        response.put("filters", filters);
        response.put("count", results.size());
        response.put("results", results);

        ctx.json(response);
    }
}
