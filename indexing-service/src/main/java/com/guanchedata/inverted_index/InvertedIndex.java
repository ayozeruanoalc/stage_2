package com.guanchedata.inverted_index;

import java.util.List;
import java.util.Map;

public interface InvertedIndex {
    void saveIndexForBook(int bookId, Map<String, List<Integer>> positionDict);

}
