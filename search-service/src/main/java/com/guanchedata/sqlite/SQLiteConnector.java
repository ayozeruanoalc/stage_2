package com.guanchedata.sqlite;

import java.sql.*;
import java.util.*;

public class SQLiteConnector {
    private final String url;

    public SQLiteConnector(String url) {
        this.url = "jdbc:sqlite:" + url;
    }

    public List<Map<String, Object>> findMetadata(List<Integer> ids, Map<String, Object> filters) {
        List<Map<String, Object>> results = new ArrayList<>();

        if (ids.isEmpty()) return results;

        String placeholders = String.join(",", Collections.nCopies(ids.size(), "?"));
        StringBuilder sql = new StringBuilder("SELECT id, title, author, language, year FROM metadata WHERE id IN ( " + placeholders + " )" );

        List<Object> params = new ArrayList<>(ids);

        if (filters != null && !filters.isEmpty()) {
            for (String key : filters.keySet()) {
                Object value = filters.get(key);
                if (value != null) {
                    switch (key) {
                        case "author":
                            String authorString = value.toString();
                            if (authorString.contains(",") &&
                                    Arrays.asList(authorString.split(",")).size() > 1) {
                                String[] authors = authorString.split(",");
                                sql.append(" AND (");
                                for (int i = 0; i < authors.length; i++) {
                                    if (i > 0) sql.append(" OR ");
                                    sql.append("author LIKE ?");
                                    params.add("%" + authors[i].trim() + "%");
                                }
                                sql.append(")");
                            } else {
                                sql.append(" AND author LIKE ?");
                                params.add("%" + authorString.trim() + "%");
                            }
                            break;
                        case "language":
                            if (value.toString().contains(",") &&
                                    Arrays.asList(value.toString().split(",")).size() > 1) {
                                List<String> values = Arrays.asList(value.toString().split(","));
                                for (String valueSplit : values) {
                                    sql.append(" AND language LIKE ?");
                                    params.add("%" + valueSplit + "%");
                                }
                            } else {
                                sql.append(" AND language LIKE ?");
                                params.add("%" + value + "%");
                            }
                            break;
                        case "year":
                            String yearString = value.toString();
                            if (yearString.contains(",") &&
                                    Arrays.asList(yearString.split(",")).size() > 1) {
                                String[] years = yearString.split(",");
                                String placeholdersYears = String.join(",", Collections.nCopies(years.length, "?"));
                                sql.append(" AND year IN (" + placeholdersYears + ") ");
                                for (String year : years) {
                                    params.add(Integer.parseInt(year.trim()));
                                }
                            } else {
                                sql.append(" AND year = ?");
                                params.add(value);
                            }
                            break;
                        default:
                            sql.append(" AND ").append(key).append(" = ?");
                            params.add(value);
                    }

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
                Map<String, Object> row = new HashMap<>();
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
}
