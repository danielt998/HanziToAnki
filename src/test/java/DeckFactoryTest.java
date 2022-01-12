import dictionary.Extract;
import hanziToAnki.ChineseDeckFactory;
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

        var deckFactory = new ChineseDeckFactory();
        var deck = deckFactory.generateDeck(words);
        String deckString = String.join("\n", deck);

        var resStream = this.getClass().getResourceAsStream("validWordsDeck.txt");
        var expected = new String(resStream.readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(expected, deckString);
    }

}
