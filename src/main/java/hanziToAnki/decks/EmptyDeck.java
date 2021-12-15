package hanziToAnki.decks;

import dictionary.Word;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class EmptyDeck implements Deck {

    @Override
    public void generate(Set<Word> words) {
        return;
    }

    @Override
    public List<String> getLines() {
        return Collections.emptyList();
    }
}
