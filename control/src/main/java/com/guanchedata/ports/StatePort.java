package com.guanchedata.ports;

import java.io.IOException;

public interface StatePort {
    void markIngesting(int bookId) throws IOException;
    void markIndexing(int bookId) throws IOException;
    void markIndexed(int bookId) throws IOException;
    void markError(int bookId, String errorCode);
    int size();
}
