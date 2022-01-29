import hanziToAnki.chinese.ChineseWord;
import hanziToAnki.Word;
import fixtures.WordFixtures;
import hanziToAnki.DeckStylerFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Set;

class DeckStylerTest {

    @Test
    void readsLinesFromFile() {
        ChineseWord word1 = WordFixtures.aWord();
        ChineseWord word2 = WordFixtures.aWord();

        Set<Word> words = Set.of(word1, word2);
        var deck = DeckStylerFactory.getDeck(words);

        Assertions.assertEquals(2, deck.style(words).size());
    }
}