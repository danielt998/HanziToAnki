package chinese;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import hanziToAnki.chinese.ChineseDictionaryExtractor;
import hanziToAnki.chinese.ChineseWord;
import java.net.URISyntaxException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DictionaryExtractorTest {

    private static ChineseDictionaryExtractor extractor;

    @BeforeAll
    static void setUp() throws URISyntaxException {
        extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();
    }

    @Test
    @DisplayName("Extract single word by simplified character")
    void canExtractSomeWord() {
        var word = extractor.getWord("哦");
        assertNotNull(word);
        assertTrue(word.isPresent());
    }

    @Test
    @DisplayName("Extract word with Er-hua suffix fallback")
    void canExtractErHuaTest() {
        var word = extractor.getWord("奴儿");
        assertTrue(word.isPresent());
        assertEquals(extractor.getWord("奴"), word);
    }

    @Test
    @DisplayName("Lookup by either Simplified or Traditional returns identical word mapping")
    void canExtractBySimplifiedAndTraditional() {
        Optional<?> simplifiedLookup = extractor.getWord("爱");
        Optional<?> traditionalLookup = extractor.getWord("愛");

        assertTrue(simplifiedLookup.isPresent(), "Simplified '爱' should be present in dictionary");
        assertTrue(traditionalLookup.isPresent(), "Traditional '愛' should be present in dictionary");
        assertEquals(simplifiedLookup, traditionalLookup, "Simplified and Traditional lookups should resolve to the same word");

        ChineseWord word = (ChineseWord) simplifiedLookup.get();
        assertEquals("爱", word.simplified());
        assertEquals("愛", word.traditional());
        assertEquals("ai", word.pinyin());
        assertEquals("ai4", word.pinyinTones());
        assertTrue(word.definition().contains("to love"));
    }

    @Test
    @DisplayName("Extract multi-character words with pinyin tones and definitions")
    void canExtractMultiCharacterWord() {
        Optional<?> wordOpt = extractor.getWord("你好");

        assertTrue(wordOpt.isPresent(), "Multi-character word '你好' should be present");
        ChineseWord word = (ChineseWord) wordOpt.get();
        assertEquals("你好", word.simplified());
        assertEquals("ni3 hao3", word.pinyinTones());
        assertEquals("nihao", word.pinyin());
        assertTrue(word.definition().contains("Hello") || word.definition().contains("hello"));
    }

    @Test
    @DisplayName("Return Optional.empty for words not present in dictionary or empty input")
    void returnsEmptyForUnknownWord() {
        assertTrue(extractor.getWord("xyzabc").isEmpty(), "Nonexistent word should return empty");
        assertTrue(extractor.getWord("").isEmpty(), "Empty string should return empty");
    }
}
