package hanziToAnki;

import java.net.URISyntaxException;
import java.util.Optional;

public interface DictionaryExtractor {

    void readInDictionary() throws URISyntaxException;

    Optional<Word> getWord(String s);

    Optional<Word> getWord(char c);

}
