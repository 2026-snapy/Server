package com.gbsw.snapy.global.filter;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BannedWordFilter {

    private static final String BANNED_WORDS_PATH = "filter/banned-words.txt";

    private Set<String> bannedWords;

    @PostConstruct
    public void init() throws IOException {
        ClassPathResource resource = new ClassPathResource(BANNED_WORDS_PATH);
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            bannedWords = reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .map(String::toLowerCase)
                    .collect(Collectors.toUnmodifiableSet());
        }
        log.info("BannedWordFilter loaded {} words", bannedWords.size());
    }

    public boolean containsBannedWord(String input) {
        if (input == null || input.isBlank()) {
            return false;
        }
        String normalized = input.toLowerCase().replaceAll("\\s+", "");
        return bannedWords.stream().anyMatch(normalized::contains);
    }
}
