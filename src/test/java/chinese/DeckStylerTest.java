package chinese;

import hanziToAnki.*;
import hanziToAnki.chinese.ChineseDeckStyler;
import hanziToAnki.chinese.ChineseWord;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DeckStylerTest {

    @Test
    void correctStylingTest() throws IOException {
        Word aiWord = new ChineseWord("爱", "爱", "ai", "ai4", "to love/to be fond of/to like/affection/to be inclined (to do sth)/to tend to (happen)/");
        Word baWord = new ChineseWord("吧", "吧", "ba", "ba5", "(modal particle indicating suggestion or surmise)/...right?/...OK?/...I presume./");
        Word ayiWord = new ChineseWord("阿姨", "阿姨", "ayi", "a1 yi2", "maternal aunt/step-mother/childcare worker/nursemaid/woman of similar age to one's parents (term of address used by child)/CL:個|个[ge4]/");

        LinkedHashSet<Word> words = new LinkedHashSet<>(); // ensures fixed order
        words.add(aiWord);
        words.add(baWord);
        words.add(ayiWord);

        var deckStyler = new ChineseDeckStyler();
        var styledLines = deckStyler.style(words);
        String deckString = String.join("\n", styledLines);

        var resStream = this.getClass().getResourceAsStream("/validWordsDeck.txt");
        var expected = new String(resStream.readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(expected, deckString);
    }
}
