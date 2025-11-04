package com.guanchedata.infrastructure.ports;

import java.util.Set;

public interface IndexSizeCalculator {
    String getTotalIndexSizeMB();

    Set<Integer> getAlreadyIndexedBooksSet();
}
