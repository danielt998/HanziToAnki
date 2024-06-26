package hanziToAnki.chinese;

import hanziToAnki.DictionaryExtractor;
import hanziToAnki.Grader;
import hanziToAnki.Word;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ChineseGrader implements Grader {
    private static final String VOCAB_DIRECTORY = "vocab_lists/HSK";

    private final DictionaryExtractor extractor;

    public ChineseGrader(DictionaryExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    public Set<Word> getAccumulativeVocabulary(int hskLevel) {
        Set<Word> accumulativeVocabulary = new HashSet<>();
        for (int level = 1; level <= hskLevel; level++) {
            accumulativeVocabulary.addAll(getHskVocabularyOneLevel(level));
        }
        return accumulativeVocabulary;
    }

    private Set<Word> getHskVocabularyOneLevel(int level) {
        return getWordsFromNewlineSeparatedFile(VOCAB_DIRECTORY + level);
    }

    private Set<Word> getWordsFromNewlineSeparatedFile(String filename) {
        try {
            URI uri = ChineseGrader.class.getClassLoader().getResource(filename).toURI();
            Path path = Path.of(uri);
            return Files.readAllLines(path).stream()
                    .map(extractor::getWord)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace(); // We should throw these up and display in GUI
            return Set.of();
        }
    }
}
