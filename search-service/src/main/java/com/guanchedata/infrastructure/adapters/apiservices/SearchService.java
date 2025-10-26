package com.guanchedata.infrastructure.adapters.apiservices;

import com.guanchedata.infrastructure.adapters.provider.index.MongoDBConnector;
import com.guanchedata.infrastructure.adapters.provider.metadata.SQLiteConnector;
import org.bson.Document;

import java.util.Collections;
import java.util.List;
import java.util.Map;
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
        List<Integer> docsIds = docs.keySet().stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());

        return sqliteConnector.findMetadata(docsIds, filters);
    }
}
