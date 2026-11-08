import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hanziToAnki.DeckProducer;
import hanziToAnki.ExportOptions;
import hanziToAnki.OutputFormat;
import hanziToAnki.chinese.ChineseDeckStyler;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import hanziToAnki.chinese.ChineseWordFinder;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DeckProducerTest {
    private static DeckProducer deckProducer;

    @BeforeAll
    static void setUp() throws URISyntaxException {
        ChineseDictionaryExtractor extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();
        deckProducer = new DeckProducer(extractor);
    }

    @Test
    @DisplayName("Empty input produces not-null result")
    void produceDeckWithEmptyInput() {
        ExportOptions options = new ExportOptions(
            false, true, 0,
            ChineseWordFinder.STRATEGY.TRI_BI_MONOGRAMS_USE_ALL_CHARS_BIGRAM_OVERLAP,
            OutputFormat.ANKI,
            ChineseDeckStyler.HanziType.SIMP
        );

        List<String> result = deckProducer.produceDeck(List.of(), options);
        assertNotNull(result);
    }

    @Test
    @DisplayName("Unsupported export format returns empty list")
    void produceDeckReturnsEmptyListForUnsupportedFormat() {
        ExportOptions options = new ExportOptions(
            false, true, 0,
            ChineseWordFinder.STRATEGY.TRI_BI_MONOGRAMS_USE_ALL_CHARS_BIGRAM_OVERLAP,
            OutputFormat.MEMRISE,
            ChineseDeckStyler.HanziType.SIMP
        );

        List<String> result = deckProducer.produceDeck(List.of("你"), options);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Valid input produces deck with #notetype header and tab-delimited word lines")
    void produceDeckWithValidInput() {
        ExportOptions options = new ExportOptions(
            false, true, 0,
            ChineseWordFinder.STRATEGY.TRI_BI_MONOGRAMS_USE_ALL_CHARS_BIGRAM_OVERLAP,
            OutputFormat.ANKI,
            ChineseDeckStyler.HanziType.SIMP
        );

        List<String> result = deckProducer.produceDeck(List.of("你好"), options);
        assertFalse(result.isEmpty(), "Deck should not be empty for valid Chinese input");
        assertEquals("#notetype:Chinese", result.get(0));

        // Line 1 should have 3 tab-delimited columns for SIMP: word, definition, pinyinHtml
        String cardLine = result.get(1);
        String[] columns = cardLine.split("\t");
        assertEquals(3, columns.length, "SIMP output should have 3 columns: hanzi, definition, pinyinHtml");
        assertEquals("你好", columns[0]);
        assertTrue(columns[2].contains("<span class=\"tone"), "Pinyin column should contain tone HTML spans");
    }

    @Test
    @DisplayName("HSK level exclusion filters out known vocabulary up to specified tier")
    void produceDeckWithHskLevelExclusion() {
        // "你好" is in HSK 1, "帮助" is in HSK 2
        List<String> input = List.of("你好帮助");

        // When hskLevelToExclude = 0: both words are kept
        ExportOptions optionsNoExclusion = new ExportOptions(
            false, true, 0,
            ChineseWordFinder.STRATEGY.TRI_BI_MONOGRAMS_USE_ALL_CHARS,
            OutputFormat.ANKI,
            ChineseDeckStyler.HanziType.SIMP
        );
        List<String> deckNoExclusion = deckProducer.produceDeck(input, optionsNoExclusion);
        String textNoExclusion = String.join("\n", deckNoExclusion);
        assertTrue(textNoExclusion.contains("你好"), "Should contain HSK 1 word when no levels excluded");
        assertTrue(textNoExclusion.contains("帮助"), "Should contain HSK 2 word when no levels excluded");

        // When hskLevelToExclude = 1: HSK 1 ("你好") is filtered out, HSK 2 ("帮助") remains
        ExportOptions optionsExcludeHsk1 = new ExportOptions(
            false, true, 1,
            ChineseWordFinder.STRATEGY.TRI_BI_MONOGRAMS_USE_ALL_CHARS,
            OutputFormat.ANKI,
            ChineseDeckStyler.HanziType.SIMP
        );
        List<String> deckExcludeHsk1 = deckProducer.produceDeck(input, optionsExcludeHsk1);
        String textExcludeHsk1 = String.join("\n", deckExcludeHsk1);
        assertFalse(textExcludeHsk1.contains("你好"), "Should exclude HSK 1 word '你好'");
        assertTrue(textExcludeHsk1.contains("帮助"), "Should keep HSK 2 word '帮助'");

        // When hskLevelToExclude = 2: both HSK 1 and HSK 2 are filtered out
        ExportOptions optionsExcludeHsk2 = new ExportOptions(
            false, true, 2,
            ChineseWordFinder.STRATEGY.TRI_BI_MONOGRAMS_USE_ALL_CHARS,
            OutputFormat.ANKI,
            ChineseDeckStyler.HanziType.SIMP
        );
        List<String> deckExcludeHsk2 = deckProducer.produceDeck(input, optionsExcludeHsk2);
        String textExcludeHsk2 = String.join("\n", deckExcludeHsk2);
        assertFalse(textExcludeHsk2.contains("你好"), "Should exclude HSK 1 word '你好'");
        assertFalse(textExcludeHsk2.contains("帮助"), "Should exclude HSK 2 word '帮助'");
    }

    @Test
    @DisplayName("SIMP_AND_TRAD output format includes both Simplified and Traditional columns")
    void produceDeckWithSimpAndTrad() {
        ExportOptions options = new ExportOptions(
            false, true, 0,
            ChineseWordFinder.STRATEGY.TRI_BI_MONOGRAMS_USE_ALL_CHARS,
            OutputFormat.ANKI,
            ChineseDeckStyler.HanziType.SIMP_AND_TRAD
        );

        List<String> result = deckProducer.produceDeck(List.of("爱"), options);
        assertFalse(result.isEmpty());
        assertEquals("#notetype:Chinese", result.get(0));

        // Line 1 should have 4 tab-delimited columns: simplified, traditional, definition, pinyinHtml
        String[] columns = result.get(1).split("\t");
        assertEquals(4, columns.length, "SIMP_AND_TRAD output should have 4 columns");
        assertEquals("爱", columns[0]);
        assertEquals("愛", columns[1]);
    }
}
