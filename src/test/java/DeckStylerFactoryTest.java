import hanziToAnki.DeckStyler;
import hanziToAnki.DictionaryExtractor;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import hanziToAnki.DeckStylerFactory;
import hanziToAnki.EmptyDeckStyler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeckStylerFactoryTest {

    @Test
    void testSomeValidWords() throws IOException, URISyntaxException {
        DictionaryExtractor extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();

        var words = Stream.of("爱", "吧", "阿姨")
                .map(s-> extractor.getWord(s))
                .collect(Collectors.toSet());

        DeckStyler deckStyler = DeckStylerFactory.getDeck(words);
        String deckString = String.join("\n", deckStyler.style(words));

        var resStream = this.getClass().getResourceAsStream("validWordsDeck.txt");
        var expected = new String(resStream.readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(expected, deckString);
    }

    @Test
    void noWordsGivesEmptyDeck() {
        DeckStyler deckStyler = DeckStylerFactory.getDeck(null);
        Assertions.assertTrue(deckStyler instanceof EmptyDeckStyler);
    }

}
