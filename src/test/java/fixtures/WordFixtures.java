package fixtures;

import dictionary.Word;
import org.junit.platform.commons.util.StringUtils;

public class WordFixtures {

    public static Word aWord() {
        return new Word(
        "traditional",
                "simplified",
                "nihao",
                "ni3hao3",
                "def" + Math.random()
        );
    }
}
