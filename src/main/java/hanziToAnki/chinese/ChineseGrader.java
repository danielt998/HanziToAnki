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
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChineseGrader implements Grader {
    private static final Logger logger = LoggerFactory.getLogger(ChineseGrader.class);
    private static final String VOCAB_DIRECTORY = "vocab_lists/HSK";

    private final DictionaryExtractor extractor;

    public ChineseGrader(DictionaryExtractor extractor) {
        this.extractor = extractor;
    }

    @Override
    public Set<Word> getAccumulativeVocabulary(int hskLevel) {
        Set<Word> accumulativeVocabulary = new HashSet<>();
        for (int level = 1; level <= hskLevel; level++) {
            accumulativeVocabulary.addAll(getVocabulary(level));
        }
        return accumulativeVocabulary;
    }

    public Set<Word> getVocabulary(int level) {
        if (level < 1 || level > 6) {
            throw new IllegalArgumentException("HSK level must be between 1 and 6");
        }
        return getWordsFromNewlineSeparatedFile(VOCAB_DIRECTORY + level);
    }

    private Set<Word> getWordsFromNewlineSeparatedFile(String filename) {
        try {
            URI uri = ChineseGrader.class.getClassLoader().getResource(filename).toURI();
            Path path = Path.of(uri);
            return Files.readAllLines(path).stream()
                    .map(extractor::getWord)
                    .flatMap(java.util.Optional::stream)
                    .collect(Collectors.toSet());
        } catch (URISyntaxException | IOException e) {
            logger.error("Failed to load vocabulary from file: {}", filename, e);
            return Set.of();
        }
    }
}
