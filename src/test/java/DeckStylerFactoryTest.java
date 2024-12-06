import hanziToAnki.DeckStyler;
import hanziToAnki.DeckStylerFactory;
import hanziToAnki.EmptyDeckStyler;
import hanziToAnki.chinese.ChineseDeckStyler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DeckStylerFactoryTest {
    @Test
    void noWordsGivesEmptyDeck() {
        DeckStyler deckStyler = DeckStylerFactory.getDeckStyler(null, ChineseDeckStyler.HanziType.SIMP);
        Assertions.assertTrue(deckStyler instanceof EmptyDeckStyler);
    }
}