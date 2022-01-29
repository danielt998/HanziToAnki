package hanziToAnki.decks;

import dictionary.Word;

import java.util.List;
import java.util.Set;

public class EmptyDeck implements Deck {

    @Override
    public List<String> generate(Set<Word> words) {
        return List.of();
    }
}
