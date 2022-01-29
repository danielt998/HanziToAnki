package hanziToAnki;

import java.util.List;
import java.util.Set;

public class EmptyDeckStyler implements DeckStyler {

    @Override
    public List<String> style(Set<Word> words) {
        return List.of();
    }
}
