package dictionary;

import hanziToAnki.DictionaryExtractor;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Objects;
import java.util.stream.Stream;

class ChineseDictionaryExtractorTest {

    @Test
    void canExtractErHuaTest() throws URISyntaxException {
        DictionaryExtractor extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();

        // 奴儿 is not in the dictionary; 奴 is
        var word = Stream.of("奴儿")
            .map(extractor::getWord)
            .filter(Objects::nonNull) // Extract returns null if neither simp nor trad maps have the word
            .findFirst()
            .get();

        Assertions.assertEquals(extractor.getWord("奴"), word);
    }

}