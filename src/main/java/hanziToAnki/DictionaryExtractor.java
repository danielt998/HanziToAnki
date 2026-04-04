package hanziToAnki;

import java.util.Optional;

public interface DictionaryExtractor {

    void readInDictionary();

    Optional<Word> getWord(String s);

    Optional<Word> getWord(char c);

}
