package hanziToAnki.deckStyler;

import hanziToAnki.Word;
import hanziToAnki.chinese.ChineseAnkiDeckStyler;
import hanziToAnki.chinese.ChinesePaperDeckStyler;
import hanziToAnki.chinese.ChineseWord;

import java.util.Set;

public class DeckStylerFactory {

    public static PaperDeckStyler getPaperDeckStyler(Set<Word> words) {
         if (areChinese(words)) {
            return new ChinesePaperDeckStyler();
        } else {
            return new EmptyPaperDeckStyler();
        }
    }

    public static PlainTextDeckStyler getDeck(Set<Word> words) {
        if (areChinese(words)) {
            return new ChineseAnkiDeckStyler();
        } else {
            return new EmptyPlainTextDeckStyler();
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
