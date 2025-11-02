package com.guanchedata.application.usecases.searchservice;

import com.guanchedata.infrastructure.ports.BookSearchProvider;
import io.javalin.http.Context;
import java.util.*;

public class SearchController {
    private final BookSearchProvider searchService;

    public SearchController(BookSearchProvider searchService) {
        this.searchService = searchService;
    }
    public void getSearch(Context ctx) {
        String query = ctx.queryParam("q");
        if (query == null || query.isEmpty()) {
            ctx.status(400).json(Map.of("error", "Missing required parameter 'q'"));
            return;
        }

        Map<String, Object> filters = buildFilters(ctx);
        List<Map<String, Object>> results = executeSearch(query, filters);
        Map<String, Object> response = buildResponse(query, filters, results);

        ctx.json(response);
    }

    private Map<String, Object> buildFilters(Context ctx) {
        Map<String, Object> filters = new HashMap<>();
        String author = ctx.queryParam("author");
        String language = ctx.queryParam("language");
        String year = ctx.queryParam("year");

        if (author != null && !author.isEmpty()) filters.put("author", author);
        if (language != null && !language.isEmpty()) filters.put("language", language);
        if (year != null && !year.isEmpty()) filters.put("year", year);

        return filters;
    }

    private List<Map<String, Object>> executeSearch(String query, Map<String, Object> filters) {
        if (query.contains(",") && Arrays.asList(query.split(",")).size() > 1) {
            var queries = Arrays.asList(query.split(","));
            List<List<Map<String, Object>>> resultsList = new ArrayList<>();
            for (String q : queries) {
                resultsList.add(searchService.search(q, filters));
            }
            return filterCommonTitles(resultsList);
        } else {
            return searchService.search(query, filters);
        }
    }

    private List<Map<String, Object>> filterCommonTitles(List<List<Map<String, Object>>> resultsList) {
        Set<String> commonTitles = new HashSet<>();
        resultsList.get(0).forEach(map -> commonTitles.add(map.get("title").toString()));

        for (int i = 1; i < resultsList.size(); i++) {
            Set<String> titles = new HashSet<>();
            for (Map<String, Object> map : resultsList.get(i)) {
                titles.add(map.get("title").toString());
            }
            commonTitles.retainAll(titles);
        }

        return resultsList.get(0).stream()
                .filter(map -> commonTitles.contains(map.get("title").toString()))
                .toList();
    }

    private Map<String, Object> buildResponse(String query, Map<String, Object> filters, List<Map<String, Object>> results) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("query", query);
        response.put("filters", filters);
        response.put("count", results.size());
        response.put("results", results);
        return response;
    }
}
