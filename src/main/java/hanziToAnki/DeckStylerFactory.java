package hanziToAnki;

import hanziToAnki.chinese.ChineseDeckStyler;
import hanziToAnki.chinese.ChineseWord;

import java.util.Set;

public class DeckStylerFactory {

    public static DeckStyler getDeck(Set<Word> words) {
        if (areChinese(words)) {
            return new ChineseDeckStyler();
        } else {
            return new EmptyDeckStyler();
        }
    }

    private static boolean areChinese(Set<Word> words) {
        try {
            return words.stream().allMatch(w -> w instanceof ChineseWord);
        } catch (NullPointerException e) {
            return false; // hack for now, until we have Word interface and implementations
        }
    }
}
