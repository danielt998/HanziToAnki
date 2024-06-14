package hanziToAnki.deckStyler;

import hanziToAnki.Word;

import java.util.List;
import java.util.Set;

public interface PlainTextDeckStyler extends DeckStyler {
    List<String> style(Set<Word> words);
}
