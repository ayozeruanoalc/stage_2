package com.guanchedata.infrastructure.ports;

import org.bson.Document;

public interface InvertedIndexProvider {
    Document findWord(String word);
}
