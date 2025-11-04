package com.guanchedata.infrastructure.ports;

import java.util.List;
import java.util.Map;

public interface ResultsSorter {
    public void sort(List<Map<String, Object>> results);
}
