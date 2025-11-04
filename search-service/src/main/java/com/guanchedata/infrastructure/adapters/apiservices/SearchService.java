package com.guanchedata.infrastructure.adapters.apiservices;

import com.guanchedata.infrastructure.ports.BookSearchProvider;
import com.guanchedata.infrastructure.ports.MetadataProvider;
import com.guanchedata.infrastructure.ports.InvertedIndexProvider;
import com.guanchedata.infrastructure.ports.ResultsSorter;
import org.bson.Document;

import java.util.*;
import java.util.stream.Collectors;

public class SearchService implements BookSearchProvider {
    private final InvertedIndexProvider invertedIndexConnector;
    private final MetadataProvider metadataConnector;
    private final ResultsSorter resultsSorter;

    public SearchService(InvertedIndexProvider invertedIndexConnector, MetadataProvider metadataConnector, ResultsSorter resultsSorter) {
        this.metadataConnector = metadataConnector;
        this.invertedIndexConnector = invertedIndexConnector;
        this.resultsSorter = resultsSorter;
    }

    public Map<Integer, Integer> getFrequencies(Document document) {
        Map<Integer, Integer> frequencies = new HashMap<>();
        for (String key : document.keySet()) {
            Document subDocument = (Document) document.get(key);
            Integer frequency = subDocument.getInteger("frequency");
            frequencies.put(Integer.parseInt(key), frequency);
        }
        return frequencies;
    }

    public Document getDocByWord(String word) {
        Document wordDocument = invertedIndexConnector.findWord(word.toLowerCase());
        if (wordDocument == null) return null;
        return (Document) wordDocument.get("documents");
    }

    public List<Integer> getBooksContainsWord(String word){
        Document docs = getDocByWord(word);
        return docs.keySet().stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    public List<Map<String, Object>> search(String word, Map<String, Object> filters) {
        Document docs = getDocByWord(word);
        if (docs == null) return Collections.emptyList();

        List<Integer> docsIds = getBooksContainsWord(word);

        Map<Integer, Integer> frequencies = getFrequencies(docs);

        List<Map<String, Object>> results = metadataConnector.findMetadata(docsIds, filters);

        results.forEach(map -> map.put("frequency", frequencies.get(map.get("id"))));

        resultsSorter.sort(results);

        return results;
    }
}
