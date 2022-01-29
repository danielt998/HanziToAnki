package fixtures;

import dictionary.ChineseWord;

public class WordFixtures {

    public static ChineseWord aWord() {
        return new ChineseWord(
        "traditional",
                "simplified",
                "nihao",
                "ni3hao3",
                "def" + Math.random()
        );
    }
}
