package hanziToAnki.decks;

import dictionary.Word;

import java.util.List;
import java.util.Set;

public interface Deck {

    List<String> generate(Set<Word> words);
}
