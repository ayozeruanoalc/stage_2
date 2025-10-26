package com.guanchedata.infrastructure.ports;

import org.bson.Document;

public interface WordProvider {
    Document findWord(String word);
}
