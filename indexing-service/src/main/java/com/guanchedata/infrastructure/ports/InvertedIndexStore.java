package com.guanchedata.infrastructure.ports;

import java.util.List;
import java.util.Map;

public interface InvertedIndexStore {
    void saveIndexForBook(int bookId, Map<String, List<Integer>> positionDict);

}
