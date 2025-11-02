package com.guanchedata.benchmark.indexingservice.databaseinsertion;

import com.guanchedata.infrastructure.adapters.storer.metadata.MetadataSQLiteDB;
import com.guanchedata.infrastructure.adapters.storer.metadata.MetadataParser;
import org.openjdk.jmh.annotations.*;

import java.util.concurrent.TimeUnit;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 5)
@Measurement(iterations = 10)
@Fork(value=1, jvmArgs = {"-Xmx4G"})
@State(Scope.Thread)
public class MetadataDatabaseInsertionBenchmark {

    @Param({""})
    private String datalakePath;

    //ruta metadata.db
    @Param({""})
    private String metadataPath;

    @Param({""})
    private String idBook;


    private MetadataSQLiteDB metadataDB;
    private Connection conn;


    @Setup(Level.Trial)
    public void setup() {
        try {
            conn = DriverManager.getConnection("jdbc:sqlite:" + metadataPath);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        MetadataParser parser = new MetadataParser(datalakePath);
        metadataDB = new MetadataSQLiteDB(parser, metadataPath);
    }

    @Benchmark
    public void benchmarkSaveMetadata() {
        metadataDB.saveMetadata(Integer.parseInt(idBook));
    }

    @TearDown(Level.Invocation)
    public void tearDown() {
        try {
            String sql = "DELETE FROM metadata   WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, Integer.parseInt(idBook));
                pstmt.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    @TearDown(Level.Trial)
    public void closeConnection() {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
