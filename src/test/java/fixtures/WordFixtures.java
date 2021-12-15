package fixtures;

import dictionary.Word;

public class WordFixtures {

    public static Word aWord() {
        return new Word(
        "traditional",
                "simplified",
                "pinyin",
                "pinyinTones",
                "definition"
        );
    }
}
