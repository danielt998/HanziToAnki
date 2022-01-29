package hanziToAnki;

import java.util.List;
import java.util.Set;

public interface DeckStyler {

    List<String> style(Set<Word> words);
}
