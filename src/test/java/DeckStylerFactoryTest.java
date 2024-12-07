import hanziToAnki.DeckStyler;
import hanziToAnki.DeckStylerFactory;
import hanziToAnki.EmptyDeckStyler;
import hanziToAnki.chinese.ChineseDeckStyler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DeckStylerFactoryTest {
    @ParameterizedTest
    @EnumSource(ChineseDeckStyler.HanziType.class)
    void noWordsGivesEmptyDeck(ChineseDeckStyler.HanziType hanziType) {
        DeckStyler deckStyler = DeckStylerFactory.getDeckStyler(null, hanziType);
        Assertions.assertTrue(deckStyler instanceof EmptyDeckStyler);
    }
}