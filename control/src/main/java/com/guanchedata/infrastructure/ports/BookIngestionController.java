package com.guanchedata.infrastructure.ports;

public interface BookIngestionController {
    void triggerIngestion(int bookId) throws Exception;
    String getStatus(int bookId) throws Exception;
}
