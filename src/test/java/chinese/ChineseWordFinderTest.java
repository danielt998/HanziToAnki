package chinese;

import hanziToAnki.Word;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import hanziToAnki.chinese.ChineseWordFinder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;


class ChineseWordFinderTest {
    private static ChineseWordFinder finder;
    private static ChineseDictionaryExtractor extractor;

    @BeforeAll
    public static void setup() throws URISyntaxException {
        extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();
        finder = new ChineseWordFinder(extractor);
    }

    @Test
    void singleCharOnlyTest() {
        Set<Word> words = finder.findWords(ChineseWordFinder.Strategy.SINGLE_CHAR_ONLY, List.of("中国人"));
        assertEquals(new HashSet(Arrays.asList(extractor.getWord("中"),
                        extractor.getWord("国"),
                        extractor.getWord("人"))),
                words);
    }

    @Test
    void defaultStrategyReturnsTrigram() {

    }

    @Test
    void defaultStrategyReturnsMonogramPlusBigram() {

    }

    @Test
    void defaultStrategyReturnsBigramPlusMonogram() {

    }

    @Test
    void defaultStrategyReturnsMonograms() {

    }

    @Test
    void allCombinationsTwoOrMoreTest() {
        Set<Word> words = finder.findWords(ChineseWordFinder.Strategy.ALL_COMBINATIONS_TWO_OR_MORE, List.of("中国人"));
        assertEquals(new HashSet(Arrays.asList(extractor.getWord("中国人"),
                        extractor.getWord("中国"),
                        extractor.getWord("国人"))),
                words);
    }

    @Test
    void allCombinationsTwoOrMoreFallbackToSingle_NoFallbackTest() {
        Set<Word> words = finder.findWords(ChineseWordFinder.Strategy.ALL_COMBINATIONS_TWO_OR_MORE, List.of("中国人"));
        assertEquals(new HashSet(Arrays.asList(extractor.getWord("中国人"),
                        extractor.getWord("中国"),
                        extractor.getWord("国人"))),
                words);
    }

    @Test
    void allCombinationsTwoOrMoreFallbackToSingle_WithFallbackTest() {
        Set<Word> words = finder.findWords(ChineseWordFinder.Strategy.ALL_COMBINATIONS_TWO_OR_MORE_FALLBACK_TO_SINGLE, List.of("我是人"));
        assertEquals(new HashSet(Arrays.asList(extractor.getWord("我"),
                        extractor.getWord("是"),
                        extractor.getWord("人"))),
                words);
    }

    @Test
    void allCombinationsTest() {
        Set<Word> words = finder.findWords(ChineseWordFinder.Strategy.ALL_COMBINATIONS, List.of("中国人"));
        assertEquals(new HashSet(Arrays.asList(extractor.getWord("中国人"),
                        extractor.getWord("中国"),
                        extractor.getWord("国人"),
                        extractor.getWord("中"),
                        extractor.getWord("国"),
                        extractor.getWord("人"))),
                words);
    }

    @Test
    void longestWordsOnlyTest_ThreeChars() {
        Set<Word> words = finder.findWords(ChineseWordFinder.Strategy.LONGEST_WORDS_ONLY, List.of("中国人"));
        assertEquals(new HashSet(Arrays.asList(extractor.getWord("中国人"))), words);
    }

    @Test
    void longestWordsOnlyTest_TwoChars() {
        Set<Word> words = finder.findWords(ChineseWordFinder.Strategy.LONGEST_WORDS_ONLY, List.of("中国是"));
        assertEquals(new HashSet(Arrays.asList(extractor.getWord("中国"))), words);
    }

    @Test
    void longestWordsOnlyTest_OneChar() {
        Set<Word> words = finder.findWords(ChineseWordFinder.Strategy.LONGEST_WORDS_ONLY, List.of("我是人"));
        assertEquals(new HashSet(Arrays.asList(extractor.getWord("我"),
                        extractor.getWord("是"),
                        extractor.getWord("人"))),
                words);
    }
}