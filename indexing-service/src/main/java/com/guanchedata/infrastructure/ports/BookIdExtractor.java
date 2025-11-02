package com.guanchedata.infrastructure.ports;

import java.util.HashSet;

public interface BookIdExtractor {
    HashSet<Integer> generateDownloadedBooksSet();
}
