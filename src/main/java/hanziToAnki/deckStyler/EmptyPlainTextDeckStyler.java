package hanziToAnki.deckStyler;

import hanziToAnki.Word;

import java.util.List;
import java.util.Set;

public class EmptyPlainTextDeckStyler implements PlainTextDeckStyler {
    @Override
    public List<String> style(Set<Word> words) {
        return null;
    }
}
