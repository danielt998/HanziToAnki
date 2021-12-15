package hanziToAnki.decks;

import dictionary.Word;

import java.util.List;
import java.util.Set;

public interface Deck {

    public void generate(Set<Word> words);

    public List<String> getLines();
}
