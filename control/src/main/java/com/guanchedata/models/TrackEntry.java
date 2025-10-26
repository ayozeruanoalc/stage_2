package com.guanchedata.models;

public class TrackEntry {
    public int bookId;
    public PipelineStage stage;
    public String path;
    public String lastError;
    public long updatedAt;

    public TrackEntry(int bookId, PipelineStage stage) {
        this.bookId = bookId;
        this.stage = stage;
        this.updatedAt = System.currentTimeMillis();
    }
}
