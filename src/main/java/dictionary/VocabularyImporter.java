package dictionary;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class VocabularyImporter {
    private static final String VOCAB_DIRECTORY = "vocab_lists/HSK";

    public static Set<Word> getAccumulativeHSKVocabulary(int HSKLevel) {
        Set<Word> accumulativeVocabulary = new HashSet<>();
        for (int level = 1; level <= HSKLevel; level++) {
            accumulativeVocabulary.addAll(getHSKVocabularyOneLevel(level));
        }
        return accumulativeVocabulary;
    }

    private static Set<Word> getHSKVocabularyOneLevel(int level) {
        return getWordsFromNewlineSeparatedFile(VOCAB_DIRECTORY + level);
    }

    private static Set<Word> getWordsFromNewlineSeparatedFile(String filename) {
        try {
            URI uri = VocabularyImporter.class.getClassLoader().getResource(filename).toURI();
            Path path = Path.of(uri);
            return getWordsFromStringList(Files.readAllLines(path));
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace(); // We should throw these up and display in GUI
            return Set.of();
        }
    }

    public static Set<Word> getWordsFromStringList(List<String> lines) {
        return lines.stream()
                .map(Extract::getWordFromChinese)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }
}
