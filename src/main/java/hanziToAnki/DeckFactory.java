package hanziToAnki;

import dictionary.Word;

import java.util.List;
import java.util.Set;

public interface DeckFactory {
    public List<String> generateDeck(Set<Word> words);
}
