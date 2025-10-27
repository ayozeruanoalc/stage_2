package com.guanchedata.infrastructure.adapters.provider.invertedindex;

import com.guanchedata.infrastructure.adapters.provider.stopwords.StopwordsLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.*;

public class BookIndexProcessor {

    public void processBookFile(Path file, Integer bookId, Map<String, String> languageReferences, String stopwordsPath, Map<String, Set<String>> stopwordsCache, MongoDBInvertedIndexStore mongoDBInstance) {
        String fileName = file.getFileName().toString();
        Matcher matcher = Pattern.compile("^(\\d+)_").matcher(fileName);

        if (!matcher.find()) return;

        int fileBookId = Integer.parseInt(matcher.group(1));
        String bookIdStr = String.valueOf(fileBookId);

        if (!bookId.equals(fileBookId) || !languageReferences.containsKey(bookIdStr)) return;

        String language = languageReferences.get(bookIdStr);
        if (language == null) return;
        language = language.toLowerCase();

        Set<String> stopWords = StopwordsLoader.loadStopwords(stopwordsPath, stopwordsCache, language);
        if (stopWords.isEmpty()) {
            System.out.println("No se cargaron stopwords para el idioma: " + language);
            return;
        }

        Map<String, List<Integer>> positionDict = extractWordPositions(file, stopWords);
        mongoDBInstance.saveIndexForBook(fileBookId, positionDict);
    }

    public Map<String, List<Integer>> extractWordPositions(Path file, Set<String> stopWords) {
        Map<String, List<Integer>> positionDict = new HashMap<>();
        int wordPosition = 0;

        try (BufferedReader br = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("[^\\p{Alpha}]+");
                for (String token : tokens) {
                    if (token.isEmpty()) continue;
                    String word = token.toLowerCase();
                    if (!stopWords.contains(word)) {
                        positionDict.computeIfAbsent(word, k -> new ArrayList<>()).add(wordPosition);
                    }
                    wordPosition++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return positionDict;
    }



}
