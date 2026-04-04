package hanziToAnki.chinese;

import hanziToAnki.DictionaryExtractor;
import hanziToAnki.Word;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extracts Chinese word definitions from CC-CEDICT dictionary format.
 * Supports both simplified and traditional Chinese characters, and handles
 * erhua (儿化) pronunciation variations.
 */
public class ChineseDictionaryExtractor implements DictionaryExtractor {
    private static final Logger logger = LoggerFactory.getLogger(ChineseDictionaryExtractor.class);
    private static final String DEFAULT_DICTIONARY_FILENAME = "cedict_ts.u8";
    private static final char COMMENT_CHARACTER = '#';

    private final Map<String, Word> simplifiedMapping = new HashMap<>();
    private final Map<String, Word> traditionalMapping = new HashMap<>();

    @Override
    public void readInDictionary() {
        InputStream dictionaryStream = getClass().getResourceAsStream("/dictionary/" + DEFAULT_DICTIONARY_FILENAME);
        if (dictionaryStream == null) {
            logger.error("Dictionary file not found: /dictionary/{}", DEFAULT_DICTIONARY_FILENAME);
            return;
        }
        readInDictionary(dictionaryStream);
    }

    private void readInDictionary(InputStream stream) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            reader.lines()
                    .filter(line -> !line.isEmpty() && line.charAt(0) != COMMENT_CHARACTER)
                    .map(this::getWordFromLine)
                    .filter(Objects::nonNull)
                    .forEach(this::putWordToMaps);
            logger.info("Successfully loaded dictionary with {} simplified and {} traditional words",
                    simplifiedMapping.size(), traditionalMapping.size());
        } catch (IOException e) {
            logger.error("Could not load dictionary file", e);
        }
    }

    @Override
    public Optional<Word> getWord(char c) {
        return getWord(String.valueOf(c));
    }

    @Override
    public Optional<Word> getWord(String s) {
        Word word = simplifiedMapping.getOrDefault(s, traditionalMapping.get(s));
        if (word != null) {
            return Optional.of(word);
        }

        if (mightBeErhua(s)) {
            String stripped = sanitiseErhua(s);
            word = simplifiedMapping.getOrDefault(stripped, traditionalMapping.get(stripped));
            if (word != null) {
                return Optional.of(word);
            }
        }

        return Optional.empty();
    }

    /**
     * Parses a CC-CEDICT format line: "Traditional Simplified [pinyin] /definition/"
     */
    private Word getWordFromLine(String line) {
        String[] definitionParts = line.split(" /");
        if (definitionParts.length < 2) {
            return null;
        }
        String definition = definitionParts[1];

        String[] pinyinParts = definitionParts[0].split("\\[");
        if (pinyinParts.length < 2) {
            return null;
        }
        String pinyinNoTones = pinyinParts[1].replaceAll("[\\[\\]12345 ]", "").toLowerCase();
        String pinyinWithTones = pinyinParts[1].replaceAll("[\\[\\]]", "").toLowerCase();

        String[] characterParts = pinyinParts[0].split(" ");
        if (characterParts.length < 2) {
            return null;
        }
        String traditional = characterParts[0];
        String simplified = characterParts[1];

        return new ChineseWord(traditional, simplified, pinyinNoTones, pinyinWithTones, definition);
    }

    private void putWordToMaps(Word word) {
        if (word instanceof ChineseWord w) {
            simplifiedMapping.put(w.simplified(), word);
            traditionalMapping.put(w.traditional(), word);
        }
    }

    private boolean mightBeErhua(String word) {
        return word.endsWith("儿");
    }

    private String sanitiseErhua(String word) {
        return word.substring(0, word.length() - 1);
    }
}
