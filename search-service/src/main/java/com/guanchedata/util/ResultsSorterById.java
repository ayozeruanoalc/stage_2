package com.guanchedata.util;

import com.guanchedata.infrastructure.ports.ResultsSorter;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class ResultsSorterById implements ResultsSorter {
    @Override
    public void sort(List<Map<String, Object>> results,  Map<Integer, Integer> frequencies) {
        results.sort(Comparator.comparingInt(a -> (Integer) a.get("id")));
    }
}
