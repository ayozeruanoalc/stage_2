package com.guanchedata.state;

public class StateEntry {
    private int bookId;
    private String stage;
    private String path;
    private String error;

    public StateEntry(int bookId, String stage, String path, String error) {
        this.bookId = bookId;
        this.stage = stage;
        this.path = path;
        this.error = error;
    }

    public int getBookId() { return bookId; }
    public String getStage() { return stage; }
    public String getPath() { return path; }
    public String getError() { return error; }

    public void setStage(String stage) { this.stage = stage; }
    public void setPath(String path) { this.path = path; }
    public void setError(String error) { this.error = error; }

    @Override
    public String toString() {
        return "StateEntry{id=" + bookId + ", stage=" + stage + ", path=" + path + ", error=" + error + "}";
    }
}
