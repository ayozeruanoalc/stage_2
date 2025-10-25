package com.guanchedata.models;

public class BookStatus {
    public int bookId;
    public String ingestionStatus;
    public String indexingStatus;
    public String path;

    public BookStatus(int bookId) {
        this.bookId = bookId;
    }
}
