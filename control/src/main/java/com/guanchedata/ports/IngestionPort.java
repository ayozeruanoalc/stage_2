package com.guanchedata.ports;

public interface IngestionPort {
    void triggerIngestion(int bookId) throws Exception;
    String getStatus(int bookId) throws Exception;
}
