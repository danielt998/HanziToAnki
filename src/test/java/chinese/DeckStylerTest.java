package chinese;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hanziToAnki.Word;
import hanziToAnki.chinese.ChineseDeckStyler;
import hanziToAnki.chinese.ChineseWord;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;


public class DeckStylerTest {

    private static LinkedHashSet<Word> words = new LinkedHashSet<>();

    @BeforeAll
    public static void populateWordList() {
        Word aiWord = new ChineseWord("愛", "爱", "ai", "ai4",
                "to love/to be fond of/to like/affection/to be inclined (to do sth)/to tend to (happen)/");
        Word baWord = new ChineseWord("吧", "吧", "ba", "ba5",
                "(modal particle indicating suggestion or surmise)/...right?/...OK?/...I presume./");
        Word ayiWord = new ChineseWord("阿姨", "阿姨", "ayi", "a1 yi2",
                "maternal aunt/step-mother/childcare worker/nursemaid/woman"
                        + " of similar age to one's parents (term of address used by child)/CL:個|个[ge4]/");

        words.add(aiWord);
        words.add(baWord);
        words.add(ayiWord);
    }

    @Test
    void correctStylingTest() throws IOException {
        var deckStyler = new ChineseDeckStyler(ChineseDeckStyler.HanziType.SIMP);
        var styledLines = deckStyler.style(words);
        String deckString = String.join("\n", styledLines);

        var resStream = this.getClass().getResourceAsStream("/validWordsDeck.txt");
        var expected = new String(resStream.readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(expected, deckString);
    }

    @Test
    void correctStylingTestWithTrad() throws IOException {
        var deckStyler = new ChineseDeckStyler(ChineseDeckStyler.HanziType.TRAD);
        var styledLines = deckStyler.style(words);
        String deckString = String.join("\n", styledLines);

        var resStream = this.getClass().getResourceAsStream("/validWordsDeckTrad.txt");
        var expected = new String(resStream.readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(expected, deckString);
    }

    @Test
    void correctStylingTestWithSimpAndTrad() throws IOException {
        var deckStyler = new ChineseDeckStyler(ChineseDeckStyler.HanziType.SIMP_AND_TRAD);
        var styledLines = deckStyler.style(words);
        String deckString = String.join("\n", styledLines);

        var resStream = this.getClass().getResourceAsStream("/validWordsDeckSimpAndTrad.txt");
        var expected = new String(resStream.readAllBytes(), StandardCharsets.UTF_8);

        assertEquals(expected, deckString);
    }
}
