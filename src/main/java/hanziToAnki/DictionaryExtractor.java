package hanziToAnki;

import java.net.URISyntaxException;

public interface DictionaryExtractor {

    void readInDictionary() throws URISyntaxException;

    Word getWord(String s);

    Word getWord(char c);

}
