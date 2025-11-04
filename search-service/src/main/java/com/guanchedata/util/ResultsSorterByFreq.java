package com.guanchedata.util;

import com.guanchedata.infrastructure.ports.ResultsSorter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ResultsSorterByFreq implements ResultsSorter {
    @Override
    public void sort(List<Map<String, Object>> results) {
        results.sort(Comparator.<Map<String, Object>>comparingInt(a -> (Integer) a.get("frequency")).reversed());
    }
}
