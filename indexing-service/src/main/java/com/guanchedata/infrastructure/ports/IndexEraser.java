package com.guanchedata.infrastructure.ports;

public interface IndexEraser {
    void erasePreviousIndex();

    void eraseMetadata();

    void eraseInvertedIndex();
}
