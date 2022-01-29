package hanziToAnki;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface DictionaryExtractor {

    HashMap<String, Word> generate();
}
