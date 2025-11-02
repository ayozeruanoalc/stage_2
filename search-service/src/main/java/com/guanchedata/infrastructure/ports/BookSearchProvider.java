package com.guanchedata.infrastructure.ports;

import java.util.List;
import java.util.Map;

public interface BookSearchProvider {
    List<Map<String, Object>> search(String word, Map<String, Object> filters);
}
