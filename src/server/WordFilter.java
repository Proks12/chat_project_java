package server;


import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WordFilter {

    private final Set<String> bannedWords = ConcurrentHashMap.newKeySet();

    public WordFilter(Path filePath) throws IOException {
        loadFromFile(filePath);
    }

    private void loadFromFile(Path path) throws IOException {
        if (!Files.exists(path)) {
            throw new IOException("Soubor banwords neexistuje: " + path);
        }

        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().toLowerCase();

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                bannedWords.add(line);
            }
        }
    }

    public boolean containsBannedWord(String message) {
        String lower = message.toLowerCase();

        for (String word : bannedWords) {
            if (lower.contains(word)) {
                return true;
            }
        }
        return false;
    }

    public Set<String> getBannedWords() {
        return bannedWords;
    }
}

