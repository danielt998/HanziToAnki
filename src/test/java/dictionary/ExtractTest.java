package dictionary;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

class ExtractTest {

    static Extract extract = new Extract();

    @BeforeAll
    static void setUp() throws URISyntaxException {
        extract.readInDictionary();
    }

    @Test
    void canExtractErHuaTest() {
        // 奴儿 is not in the dictionary; 奴 is
        var words = Stream.of("奴儿")
            .map(Extract::getWordFromChinese)
            .filter(Objects::nonNull) // Extract returns null if neither simp nor trad maps have the word
            .toList();
        Assertions.assertFalse(words.isEmpty());
    }

}