package com.guanchedata.domain.model;

public class StateEntry {
    private final int bookId;
    private String stage;
    private final String path;
    private final String error;

    public StateEntry(int bookId, String stage, String path, String error) {
        this.bookId = bookId;
        this.stage = stage;
        this.path = path;
        this.error = error;
    }

    public void setStage(String stage) { this.stage = stage; }

    @Override
    public String toString() {
        return "StateEntry{id=" + bookId + ", stage=" + stage + ", path=" + path + ", error=" + error + "}";
    }
}
