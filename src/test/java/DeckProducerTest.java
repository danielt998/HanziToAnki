import hanziToAnki.DeckProducer;
import hanziToAnki.ExportOptions;
import hanziToAnki.OutputFormat;
import hanziToAnki.chinese.ChineseDeckStyler;
import hanziToAnki.chinese.ChineseWordFinder;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.URISyntaxException;
import java.util.List;

class DeckProducerTest {
    private DeckProducer deckProducer;

    @BeforeEach
    void setUp() throws URISyntaxException {
        ChineseDictionaryExtractor extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();
        deckProducer = new DeckProducer(extractor);
    }

    @Test
    void produceDeckWithEmptyInput() {
        ExportOptions options = new ExportOptions(
            false, true, 0,
            ChineseWordFinder.STRATEGY.TRI_BI_MONOGRAMS_USE_ALL_CHARS_BIGRAM_OVERLAP,
            OutputFormat.ANKI,
            ChineseDeckStyler.HanziType.SIMP
        );

        List<String> result = deckProducer.produceDeck(List.of(), options);
        // Empty input should return empty deck or minimal deck (depends on implementation)
        Assertions.assertNotNull(result);
    }

    @Test
    void produceDeckReturnsEmptyListForUnsupportedFormat() {
        ExportOptions options = new ExportOptions(
            false, true, 0,
            ChineseWordFinder.STRATEGY.TRI_BI_MONOGRAMS_USE_ALL_CHARS_BIGRAM_OVERLAP,
            OutputFormat.MEMRISE,
            ChineseDeckStyler.HanziType.SIMP
        );

        List<String> result = deckProducer.produceDeck(List.of("你"), options);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void produceDeckWithValidInput() {
        ExportOptions options = new ExportOptions(
            false, true, 0,
            ChineseWordFinder.STRATEGY.TRI_BI_MONOGRAMS_USE_ALL_CHARS_BIGRAM_OVERLAP,
            OutputFormat.ANKI,
            ChineseDeckStyler.HanziType.SIMP
        );

        List<String> result = deckProducer.produceDeck(List.of("你好"), options);
        Assertions.assertFalse(result.isEmpty(), "Deck should not be empty for valid Chinese input");
    }
}
