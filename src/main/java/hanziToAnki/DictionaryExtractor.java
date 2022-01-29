package hanziToAnki;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public interface DictionaryExtractor {

    void readInDictionary() throws URISyntaxException;

    Word getWord(String s);
    Word getWord(char c);

}
