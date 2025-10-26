package com.guanchedata.infrastructure.ports;

import java.util.List;
import java.util.Map;

public interface MetadataProvider {
    List<Map<String, Object>> findMetadata(List<Integer> ids, Map<String, Object> filters);
}
