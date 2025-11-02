package com.guanchedata.infrastructure.adapters.provider.metadata;

import com.guanchedata.infrastructure.ports.MetadataProvider;
import java.sql.*;
import java.util.*;

public class SQLiteConnector implements MetadataProvider {
    private final String url;

    public SQLiteConnector(String url) {
        this.url = "jdbc:sqlite:" + url;
    }

    @Override
    public List<Map<String, Object>> findMetadata(List<Integer> ids, Map<String, Object> filters) {
        List<Map<String, Object>> results = new ArrayList<>();

        if (ids.isEmpty()) return results;

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        StringBuilder sql = new StringBuilder("SELECT id, title, author, language, year FROM metadata WHERE id IN ( " + placeholders + " )");

        List<Object> params = new ArrayList<>(ids);
        if (filters != null && !filters.isEmpty()) {
            for (String key : filters.keySet()) {
                Object value = filters.get(key);
                if (value != null) {
                    appendFilter(sql, params, key, value);
                }
            }
        }

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement statement = conn.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                statement.setObject(i + 1, params.get(i));
            }

            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("id", rs.getInt("id"));
                row.put("title", rs.getString("title"));
                row.put("author", rs.getString("author"));
                row.put("language", rs.getString("language"));
                row.put("year", rs.getInt("year"));
                results.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return results;
    }

    private void appendFilter(StringBuilder sql, List<Object> params, String column, Object value) {
        String[] values = value.toString().split(",");

        if (values.length > 1) {
            sql.append(" AND (");
            for (int i = 0; i < values.length; i++) {
                if (i > 0) sql.append(" OR ");
                if (column.equals("year")) {
                    sql.append(column).append(" = ?");
                    params.add(Integer.parseInt(values[i]));
                } else {
                    sql.append(column).append(" LIKE ?");
                    params.add("%" + values[i].trim() + "%");
                }
            }
            sql.append(")");
        } else {
            sql.append(" AND ");
            if (column.equals("year")) {
                sql.append(column).append(" = ?");
                params.add(Integer.parseInt(values[0].trim()));
            } else {
                sql.append(column).append(" LIKE ?");
                params.add("%" + values[0].trim() + "%");
            }
        }
    }
}