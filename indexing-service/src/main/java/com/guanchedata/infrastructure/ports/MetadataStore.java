package com.guanchedata.infrastructure.ports;

import java.util.Map;

public interface MetadataStore {
    Map<String, String> saveMetadata(int bookId);
}
