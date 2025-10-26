package com.guanchedata.inverted_index.stopwords;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class StopwordsLoader {

    private static Map<String, List<String>> allStopwords = null;


    public static Set<String> loadStopwords(String stopwordsJsonPath, Map<String, Set<String>> stopwordsCache, String language) {
        String languageCode = convertToIsoCode(language);

        if (stopwordsCache.containsKey(languageCode)) {
            return stopwordsCache.get(languageCode);
        }

        if (allStopwords == null) {
            loadStopwordsFromJson(stopwordsJsonPath);
        }

        Set<String> stopWords = new HashSet<>();
        if (allStopwords != null && allStopwords.containsKey(languageCode)) {
            List<String> words = allStopwords.get(languageCode);
            for (String word : words) {
                stopWords.add(word.toLowerCase());
            }
        } else {
            System.err.println("[STOPWORDS] No se encontraron stopwords para el idioma: " + language + " (código ISO: " + languageCode + ")");
        }

        stopwordsCache.put(languageCode, stopWords);
        return stopWords;
    }


    private static String convertToIsoCode(String language) {
        if (language == null || language.isEmpty()) {
            return language;
        }

        String lowerLanguage = language.toLowerCase().trim();

        if (lowerLanguage.length() == 2) {
            return lowerLanguage;
        }

        String isoCode = LanguageIsoMap.LANGUAGE_ISO_MAP.get(lowerLanguage);

        return isoCode != null ? isoCode : lowerLanguage;
    }


    private static void loadStopwordsFromJson(String jsonFilePath) {
        Path jsonPath = Paths.get(jsonFilePath);

        if (!Files.exists(jsonPath)) {
            System.err.println("[STOPWORDS] El archivo JSON de stopwords no existe: " + jsonFilePath);
            allStopwords = new HashMap<>();
            return;
        }

        Gson gson = new Gson();
        Type type = new TypeToken<Map<String, List<String>>>(){}.getType();

        try (FileReader reader = new FileReader(jsonPath.toFile())) {
            allStopwords = gson.fromJson(reader, type);
            System.out.println("[STOPWORDS] Cargadas stopwords para " + allStopwords.size() + " idiomas desde JSON");
        } catch (IOException e) {
            System.err.println("[STOPWORDS] Error al cargar el archivo JSON de stopwords: " + e.getMessage());
            allStopwords = new HashMap<>();
        }
    }
}