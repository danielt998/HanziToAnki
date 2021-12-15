package hanziToAnki.decks;

import dictionary.ChineseWord;

import java.util.List;
import java.util.Set;

public interface Deck {

    public void generate(Set<ChineseWord> words);

    public List<String> getLines();
}
