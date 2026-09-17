import static org.junit.jupiter.api.Assertions.assertEquals;

import hanziToAnki.DictionaryExtractor;
import hanziToAnki.VocabularySetOperations;
import hanziToAnki.Word;
import hanziToAnki.chinese.ChineseWord;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class VocabularySetOperationsTest {

    @Test
    void supportsAnkiFirstColumnsAndOrderedSetOperations() {
        ChineseWord one = new ChineseWord("一", "一", "yi", "yi1", "one");
        ChineseWord two = new ChineseWord("二", "二", "er", "er4", "two");
        DictionaryExtractor extractor = new DictionaryExtractor() {
            @Override
            public void readInDictionary() {
            }

            @Override
            public Optional<Word> getWord(String value) {
                return switch (value) {
                    case "一" -> Optional.of(one);
                    case "二" -> Optional.of(two);
                    default -> Optional.empty();
                };
            }

            @Override
            public Optional<Word> getWord(char value) {
                return getWord(String.valueOf(value));
            }
        };
        VocabularySetOperations operations = new VocabularySetOperations(extractor);

        var book = operations.fromLines(List.of("\uFEFF一\tdefinition", "二"));
        var known = operations.fromLines(List.of("二\tAnki definition"));

        assertEquals(List.of(one), List.copyOf(operations.subtract(book, known)));
        assertEquals(List.of(two), List.copyOf(operations.intersect(book, known)));
        assertEquals(List.of(one, two), List.copyOf(operations.union(book, known)));
    }
}
