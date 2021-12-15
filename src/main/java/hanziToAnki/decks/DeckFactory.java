package hanziToAnki.decks;

import dictionary.Word;

import java.util.Set;

public class DeckFactory {

    public static Deck getDeck(Set<Word> words) {
        if (areChinese(words)) {
            return new ChineseDeck();
        } else {
            return new EmptyDeck();
        }
    }

    private static boolean areChinese(Set<Word> words) {
        try {
            return words.stream().allMatch(w -> w instanceof Word);
        } catch (NullPointerException e) {
            return false; // hack for now, until we have Word interface and implementations
        }
    }
}
