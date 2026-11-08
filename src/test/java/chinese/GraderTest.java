package chinese;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hanziToAnki.DictionaryExtractor;
import hanziToAnki.Grader;
import hanziToAnki.Word;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import hanziToAnki.chinese.ChineseGrader;
import java.net.URISyntaxException;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GraderTest {
    private static Grader grader;
    private static DictionaryExtractor extractor;

    @BeforeAll
    public static void setup() throws URISyntaxException {
        extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();
        grader = new ChineseGrader(extractor);
    }

    @Test
    @DisplayName("Accumulative vocabulary sizes match expected HSK dictionary counts")
    void getAccumulativeHskVocabularyTest() {
        assertEquals(149, grader.getAccumulativeVocabulary(1).size());
        assertEquals(297, grader.getAccumulativeVocabulary(2).size());
        assertEquals(593, grader.getAccumulativeVocabulary(3).size());
        assertEquals(1188, grader.getAccumulativeVocabulary(4).size());
        assertEquals(2486, grader.getAccumulativeVocabulary(5).size());
        assertEquals(4983, grader.getAccumulativeVocabulary(6).size());
    }

    @ParameterizedTest(name = "Word ''{0}'' first appears in HSK tier {1}")
    @CsvSource({
        "北京, 1",
        "帮助, 2",
        "爱好, 3",
        "安全, 4",
        "爱惜, 5",
        "癌症, 6"
    })
    @DisplayName("Verify words first appear in their respective HSK level")
    void wordsFirstAppearInExpectedHskLevel(String wordText, int expectedFirstLevel) {
        Word word = extractor.getWord(wordText).orElseThrow();

        // Should not be in any lower tier
        for (int level = 1; level < expectedFirstLevel; level++) {
            assertFalse(grader.getAccumulativeVocabulary(level).contains(word),
                    "Word '" + wordText + "' should not be in HSK level " + level);
        }

        // Should be in expected tier and all subsequent tiers
        for (int level = expectedFirstLevel; level <= 6; level++) {
            assertTrue(grader.getAccumulativeVocabulary(level).contains(word),
                    "Word '" + wordText + "' should be present in accumulative HSK level " + level);
        }
    }

    @Test
    @DisplayName("Higher HSK tiers strictly contain all words from lower tiers")
    void higherTiersContainAllLowerTierWords() {
        for (int level = 1; level < 6; level++) {
            Set<Word> currentTier = grader.getAccumulativeVocabulary(level);
            Set<Word> nextTier = grader.getAccumulativeVocabulary(level + 1);
            assertTrue(nextTier.containsAll(currentTier),
                    "HSK level " + level + " words must be a subset of level " + (level + 1));
        }
    }
}
