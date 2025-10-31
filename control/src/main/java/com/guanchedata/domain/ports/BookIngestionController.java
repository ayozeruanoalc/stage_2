package com.guanchedata.domain.ports;

public interface BookIngestionController {
    void triggerIngestion(int bookId) throws Exception;
    String getStatus(int bookId) throws Exception;
}
