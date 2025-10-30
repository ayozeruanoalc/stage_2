package com.guanchedata.infrastructure.adapters.apiservices;

import com.guanchedata.infrastructure.ports.MetadataProvider;
import com.guanchedata.infrastructure.ports.InvertedIndexProvider;
import com.guanchedata.infrastructure.ports.ResultsSorter;
import org.bson.Document;

import java.util.*;
import java.util.stream.Collectors;

public class SearchService {
    private final InvertedIndexProvider invertedIndexConnector;
    private final MetadataProvider metadataConnector;
    private final ResultsSorter resultsSorter;

    public SearchService(InvertedIndexProvider invertedIndexConnector, MetadataProvider metadataConnector, ResultsSorter resultsSorter) {
        this.metadataConnector = metadataConnector;
        this.invertedIndexConnector = invertedIndexConnector;
        this.resultsSorter = resultsSorter;
    }

    public List<Map<String, Object>> search(String word, Map<String, Object> filters) {
        Document wordDocument = invertedIndexConnector.findWord(word.toLowerCase());
        if (wordDocument == null) return Collections.emptyList();

        Document docs = (Document) wordDocument.get("documents");

        Map<Integer, Integer> frequencies = new HashMap<>();
        for (String key: docs.keySet()) {
            Document subDoc = (Document) docs.get(key);
            Integer frequency = subDoc.getInteger("frequency");
            frequencies.put(Integer.parseInt(key), frequency);
        }

        List<Integer> docsIds = docs.keySet().stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        List<Map<String, Object>> results = metadataConnector.findMetadata(docsIds, filters);

        results.forEach(map -> map.put("frequency", frequencies.get(map.get("id"))));

        resultsSorter.sort(results, frequencies);

        return results;
    }
}
