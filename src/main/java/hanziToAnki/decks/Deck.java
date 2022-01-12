package hanziToAnki.decks;

import dictionary.ChineseWord;
import dictionary.Word;

import java.util.List;
import java.util.Set;

public interface Deck {

    public List<String> generate(Set<Word> words);
}
