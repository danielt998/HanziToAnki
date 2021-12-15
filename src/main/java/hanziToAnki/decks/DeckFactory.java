package hanziToAnki.decks;

import dictionary.Word;

import java.util.Set;

public class DeckFactory {

    public static Deck getDeck(Set<Word> words) {
        // TODO try to get instance of word, and make deck according to word instance
            // Word instanceof ChineseWord -> return new ChineseDeck(words);
        return new ChineseDeck();
    }
}
