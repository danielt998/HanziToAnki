import dictionary.Extract;
import hanziToAnki.decks.Deck;
import hanziToAnki.decks.DeckFactory;
import hanziToAnki.decks.EmptyDeck;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeckFactoryTest {

    @BeforeAll
    static void setUp() throws URISyntaxException {
        Extract.readInDictionary();
    }

    @Test
    void testSomeValidWords() throws IOException {
        var words = Stream.of("爱", "吧", "阿姨")
                .map(Extract::getWordFromChinese)
                .collect(Collectors.toSet());

        Deck deck = DeckFactory.getDeck(words);
        String deckString = String.join("\n", deck.generate(words));

        var resStream = this.getClass().getResourceAsStream("validWordsDeck.txt");
        var expected = new String(resStream.readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(expected, deckString);
    }

    @Test
    void noWordsGivesEmptyDeck() {
        Deck deck = DeckFactory.getDeck(null);
        Assertions.assertTrue(deck instanceof EmptyDeck);
    }

}
