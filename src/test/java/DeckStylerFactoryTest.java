import hanziToAnki.deckStyler.PlainTextDeckStyler;
import hanziToAnki.deckStyler.DeckStylerFactory;
import hanziToAnki.deckStyler.EmptyDeckStyler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DeckStylerFactoryTest {
    @Test
    void noWordsGivesEmptyDeck() {
        PlainTextDeckStyler deckStyler = DeckStylerFactory.getDeck(null);
        Assertions.assertTrue(deckStyler instanceof EmptyDeckStyler);
    }
}