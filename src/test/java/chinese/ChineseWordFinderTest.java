package chinese;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hanziToAnki.Word;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import hanziToAnki.chinese.ChineseWordFinder;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    @DisplayName("SINGLE_CHAR_ONLY returns only single-character monograms")
    void singleCharOnlyTest() {
        Set<Word> words = finder.findWords(ChineseWordFinder.STRATEGY.SINGLE_CHAR_ONLY, List.of("中国人"));
        assertEquals(new HashSet<>(Arrays.asList(
                extractor.getWord("中").get(),
                extractor.getWord("国").get(),
                extractor.getWord("人").get())),
                words);
    }

    @Test
    @DisplayName("TRI_BI_MONOGRAMS_USE_ALL_CHARS returns trigrams without bigrams")
    void triBiMonogramsStrategyReturnsTrigramAndNoBigrams() {
        Set<Word> words = finder.findWords(ChineseWordFinder.STRATEGY.TRI_BI_MONOGRAMS_USE_ALL_CHARS, List.of("中国人"));
        assertEquals(new HashSet<>(List.of(extractor.getWord("中国人").get())), words);
    }

    @Test
    @DisplayName("TRI_BI_MONOGRAMS_USE_ALL_CHARS_BIGRAM_OVERLAP returns trigram and sub-bigrams")
    void defaultStrategyReturnsTrigramAndBigrams() {
        Set<Word> words = finder.findWords(ChineseWordFinder.STRATEGY.TRI_BI_MONOGRAMS_USE_ALL_CHARS_BIGRAM_OVERLAP, List.of("中国人"));
        assertEquals(new HashSet<>(Arrays.asList(
                extractor.getWord("中国人").get(),
                extractor.getWord("中国").get(),
                extractor.getWord("国人").get())), words);
    }

    @Test
    @DisplayName("Default strategy extracts monogram plus bigram when no trigram exists")
    void defaultStrategyReturnsMonogramPlusBigram() {
        Set<Word> words = finder.findWords(ChineseWordFinder.STRATEGY.TRI_BI_MONOGRAMS_USE_ALL_CHARS_BIGRAM_OVERLAP, List.of("是中国"));
        assertEquals(new HashSet<>(Arrays.asList(extractor.getWord("中国").get(), extractor.getWord("是").get())), words);
    }

    @Test
    @DisplayName("Default strategy extracts bigram plus monogram when no trigram exists")
    void defaultStrategyReturnsBigramPlusMonogram() {
        Set<Word> words = finder.findWords(ChineseWordFinder.STRATEGY.TRI_BI_MONOGRAMS_USE_ALL_CHARS_BIGRAM_OVERLAP, List.of("中国是"));
        assertEquals(new HashSet<>(Arrays.asList(extractor.getWord("中国").get(), extractor.getWord("是").get())), words);
    }

    @Test
    @DisplayName("ALL_COMBINATIONS returns trigrams, bigrams, and all single characters")
    void allCombinationsTest() {
        Set<Word> words = finder.findWords(ChineseWordFinder.STRATEGY.ALL_COMBINATIONS, List.of("中国人"));
        assertEquals(new HashSet<>(Arrays.asList(
                extractor.getWord("中国人").get(),
                extractor.getWord("中国").get(),
                extractor.getWord("国人").get(),
                extractor.getWord("中").get(),
                extractor.getWord("国").get(),
                extractor.getWord("人").get())),
                words);
    }

    @Test
    @DisplayName("BIGRAM_AND_MONOGRAM_ONLY_NO_OVERLAP excludes trigrams and keeps bigrams without monogram overlap")
    void bigramAndMonogramOnlyNoOverlapTest() {
        Set<Word> words = finder.findWords(ChineseWordFinder.STRATEGY.BIGRAM_AND_MONOGRAM_ONLY_NO_OVERLAP, List.of("中国人"));
        assertEquals(new HashSet<>(Arrays.asList(
                extractor.getWord("中国").get(),
                extractor.getWord("国人").get())),
                words);
    }

    @Test
    @DisplayName("BIGRAM_AND_MONOGRAM_ONLY_OVERLAP excludes trigrams and keeps bigrams with monograms")
    void bigramAndMonogramOnlyOverlapTest() {
        Set<Word> words = finder.findWords(ChineseWordFinder.STRATEGY.BIGRAM_AND_MONOGRAM_ONLY_OVERLAP, List.of("中国人"));
        assertEquals(new HashSet<>(Arrays.asList(
                extractor.getWord("中国").get(),
                extractor.getWord("国人").get(),
                extractor.getWord("中").get(),
                extractor.getWord("国").get(),
                extractor.getWord("人").get())),
                words);
    }

    @Test
    @DisplayName("ANSJ_SEGMENTATION intelligently segments full Chinese sentences")
    void ansjSegmentationTest() {
        Set<Word> words = finder.findWords(ChineseWordFinder.STRATEGY.ANSJ_SEGMENTATION, List.of("你好世界"));
        assertTrue(words.contains(extractor.getWord("你好").get()));
        assertTrue(words.contains(extractor.getWord("世界").get()));
    }

    @Test
    @DisplayName("Filters out non-Han characters including punctuation, Latin alphabet, and numbers")
    void nonHanCharactersFilteredTest() {
        Set<Word> words = finder.findWords(ChineseWordFinder.STRATEGY.SINGLE_CHAR_ONLY, List.of("你好，世界！ Hello 123"));
        assertEquals(new HashSet<>(Arrays.asList(
                extractor.getWord("你").get(),
                extractor.getWord("好").get(),
                extractor.getWord("世").get(),
                extractor.getWord("界").get())),
                words);
    }
}
