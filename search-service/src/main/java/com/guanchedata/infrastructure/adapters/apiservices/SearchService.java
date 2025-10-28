package com.guanchedata.infrastructure.adapters.apiservices;

import com.guanchedata.infrastructure.adapters.provider.index.MongoDBConnector;
import com.guanchedata.infrastructure.adapters.provider.metadata.SQLiteConnector;
import org.bson.Document;

import java.util.*;
import java.util.stream.Collectors;

public class SearchService {
    private final MongoDBConnector mongoDBConnector;
    private final SQLiteConnector sqliteConnector;

    public SearchService(MongoDBConnector mongoDBConnector, SQLiteConnector sqliteConnector) {
        this.mongoDBConnector = mongoDBConnector;
        this.sqliteConnector = sqliteConnector;
    }

    public List<Map<String, Object>> search(String word, Map<String, Object> filters) {
        Document wordDocument = mongoDBConnector.findWord(word.toLowerCase());
        if (wordDocument == null) return Collections.emptyList();

        Document docs = (Document) wordDocument.get("documents");

        Map<Integer, Integer> frequencies = new HashMap<>();
        for (String key: docs.keySet()) {
            Document subDoc = (Document) docs.get(key);
            Integer frequency = subDoc.getInteger("frecuency");
            frequencies.put(Integer.parseInt(key), frequency);
        }

        List<Integer> docsIds = docs.keySet().stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        List<Map<String, Object>> results = sqliteConnector.findMetadata(docsIds, filters);

        results.sort((a, b) -> {
            Integer idA = (Integer) a.getOrDefault("id", 0);
            Integer idB = (Integer) b.getOrDefault("id", 0);

            Integer frequencyA = frequencies.get(idA);
            Integer frequencyB = frequencies.get(idB);

            return Integer.compare(frequencyB, frequencyA);
        });

        return results;
    }
}
