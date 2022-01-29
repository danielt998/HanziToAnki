import hanziToAnki.DeckStyler;
import hanziToAnki.DeckStylerFactory;
import hanziToAnki.EmptyDeckStyler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DeckStylerFactoryTest {
    @Test
    void noWordsGivesEmptyDeck() {
        DeckStyler deckStyler = DeckStylerFactory.getDeck(null);
        Assertions.assertTrue(deckStyler instanceof EmptyDeckStyler);
    }
}