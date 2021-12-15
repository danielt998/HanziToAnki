import dictionary.Extract;
import dictionary.Word;
import hanziToAnki.decks.ChineseDeck;
import hanziToAnki.decks.Deck;
import hanziToAnki.decks.DeckFactory;
import hanziToAnki.decks.EmptyDeck;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeckFactoryTest {

    static Extract extract = new Extract();

    @BeforeAll
    static void setUp() throws URISyntaxException {
        extract.readInDictionary();
    }

    @Test
    void testSomeValidWords() throws IOException {
//        List<String> wordStrings = List.of("爱", "吧", "阿姨");
//        Set<Word> validWords = new HashSet<>();
//        for (String wordString: wordStrings){
//            validWords.add(extract.getWordFromChinese(wordString));
//        }
//
//        Deck deck = DeckFactory.getDeck(words);
//        ChineseDeck outputDeck = ChineseDeck.generateDeck(validWords);
//        assertEquals(IOUtils.toString(this.getClass().getResourceAsStream("validWordsDeck.txt")),
//                Arrays.toString(outputDeck.getLines().toArray()).replace(",","\n"));
    }

    @Test
    void noWordsGivesEmptyDeck() {
        Deck deck = DeckFactory.getDeck(null);
        Assertions.assertTrue(deck instanceof EmptyDeck);
    }

}
