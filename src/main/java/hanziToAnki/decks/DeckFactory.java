package hanziToAnki.decks;

import dictionary.ChineseWord;

import java.util.Set;

public class DeckFactory {

    public static Deck getDeck(Set<ChineseWord> words) {
        if (areChinese(words)) {
            return new ChineseDeck();
        } else {
            return new EmptyDeck();
        }
    }

    private static boolean areChinese(Set<ChineseWord> words) {
        try {
            return words.stream().allMatch(w -> w instanceof ChineseWord);
        } catch (NullPointerException e) {
            return false; // hack for now, until we have Word interface and implementations
        }
    }
}
