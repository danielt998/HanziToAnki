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
        if (words == null || words.isEmpty()) {
            return false;
        }
        return words.stream().allMatch(w -> w instanceof ChineseWord);
    }
}
