import dictionary.ChineseWord;
import dictionary.Word;
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

        Set<Word> words = Set.of(word1, word2);
        var deck = DeckFactory.getDeck(words);

        Assertions.assertEquals(2, deck.generate(words).size());
    }
}