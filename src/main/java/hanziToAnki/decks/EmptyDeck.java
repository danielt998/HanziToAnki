package hanziToAnki.decks;

import dictionary.ChineseWord;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class EmptyDeck implements Deck {

    @Override
    public void generate(Set<ChineseWord> words) {
        return;
    }

    @Override
    public List<String> getLines() {
        return Collections.emptyList();
    }
}
