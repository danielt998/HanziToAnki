package hanziToAnki;

import hanziToAnki.chinese.ChineseDeckStyler;
import hanziToAnki.chinese.ChineseWord;
import java.util.Set;

public class DeckStylerFactory {

    public static DeckStyler getDeckStyler(Set<Word> words, ChineseDeckStyler.HanziType hanziType) {
        if (areChinese(words)) {
            return new ChineseDeckStyler(hanziType);
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
