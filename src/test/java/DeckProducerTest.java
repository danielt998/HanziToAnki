import hanziToAnki.DeckProducer;
import hanziToAnki.DictionaryExtractor;
import hanziToAnki.ExportOptions;
import hanziToAnki.OutputFormat;
import hanziToAnki.Word;
import hanziToAnki.chinese.ChineseDeckStyler;
import hanziToAnki.chinese.ChineseWordFinder;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import hanziToAnki.chinese.ChineseWord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    @Test
    void wordListPreservesInputOrderWhileRemovingDuplicates() {
        ChineseWord first = new ChineseWord("t1", "one", "p1", "p1", "d1");
        ChineseWord second = new ChineseWord("t2", "two", "p2", "p2", "d2");
        Map<String, Word> words = Map.of("first", first, "second", second);
        DictionaryExtractor extractor = new DictionaryExtractor() {
            @Override
            public void readInDictionary() {
            }

            @Override
            public Optional<Word> getWord(String value) {
                return Optional.ofNullable(words.get(value));
            }

            @Override
            public Optional<Word> getWord(char value) {
                return Optional.empty();
            }
        };
        ExportOptions options = new ExportOptions(
                true, false, 0,
                ChineseWordFinder.STRATEGY.ANSJ_SEGMENTATION,
                OutputFormat.ANKI,
                ChineseDeckStyler.HanziType.SIMP
        );

        List<String> result = new DeckProducer(extractor)
                .produceDeck(List.of("first", "second", "first"), options);

        Assertions.assertEquals(List.of("one", "two"), result.stream()
                .skip(1)
                .map(line -> line.split("\t")[0])
                .toList());
    }
}
