package chinese;

import hanziToAnki.DictionaryExtractor;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class DictionaryExtractorTest {

    @Test
    void canExtractAWord() throws URISyntaxException {
        DictionaryExtractor extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();

        var word = extractor.getWord("哦");
        Assertions.assertNotNull(word);
    }

    @Test
    void canExtractErHuaTest() throws URISyntaxException {
        DictionaryExtractor extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();

        var word = extractor.getWord("奴儿");
        Assertions.assertEquals(extractor.getWord("奴"), word);
    }

}