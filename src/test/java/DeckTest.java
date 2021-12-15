import dictionary.ChineseWord;
import fixtures.WordFixtures;
import hanziToAnki.decks.DeckFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class DeckTest {

    @Test
    void readsLinesFromFile() {
        ChineseWord word1 = WordFixtures.aWord();
        ChineseWord word2 = WordFixtures.aWord();

        var words = Set.of(word1, word2);
        var deck = DeckFactory.getDeck(words);
        deck.generate(words);

        Assertions.assertEquals(2, deck.getLines().size());
    }
}