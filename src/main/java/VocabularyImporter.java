import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VocabularyImporter {
    private static final String VOCAB_DIRECTORY = "vocab_lists/HSK";

    public static Set<Word> getAccumulativeHSKVocabulary(int HSKLevel) {
        boolean[] levelsIncluded = new boolean[6];
        for (int i = 0; i < HSKLevel; i++) {
            levelsIncluded[i] = true;
        }
        return getHSKVocabulary(levelsIncluded);
    }

    private static Set<Word> getHSKVocabulary(boolean[] HSKLevelsIncluded) {
        Set<Word> vocabSet = new HashSet();
        for (int i = 0; i < 6; i++) {
            if (HSKLevelsIncluded[i]) {
                vocabSet.addAll(getHSKVocabularyOneLevel(i + 1));
            }
        }
        return vocabSet;
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
        Set<Word> vocabSet = new HashSet<Word>();
        for (String line : lines) {
            Word wordToAdd = Extract.getWordFromChinese(line);
            if (wordToAdd != null) {
                vocabSet.add(wordToAdd);
            }
        }
        return vocabSet;
    }
}
