package dictionary;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.Objects;
import java.util.stream.Stream;

class ExtractTest {

    @BeforeAll
    static void setUp() throws URISyntaxException {
        Extract.readInDictionary();
    }

    @Test
    void canExtractErHuaTest() {
        // 奴儿 is not in the dictionary; 奴 is
        var word = Stream.of("奴儿")
            .map(Extract::getWordFromChinese)
            .filter(Objects::nonNull) // Extract returns null if neither simp nor trad maps have the word
            .findFirst()
            .get();

        Assertions.assertEquals(Extract.getWordFromChinese("奴"), word);
    }

}