package hanziToAnki.chinese;


import hanziToAnki.DictionaryExtractor;
import hanziToAnki.Word;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/*Notes:
  This is a mess of a piece of code that I pulled from another of my projects, it needs sorting out
  Also, it has some useful methods that are of no use for this particular project, so it might be nice
  to create a separate linked GitHub project or something for it later
  Another note:for now, it will only handle unique characters, not words composed of chars, making it
  somewhat less useful than it could be
*/
/*TODO:
  tidy up formatting (e.g. trailing \)
  ensure that split(" /") does not miss anything
  give some thought to how we can implement search for characters/English too...
     maybe create some sort of hashmap
  search should not require exact matches, if whole provided string is a substring of pinyin
  should be something like:
    if it's a (partial) match:
      traverse the list both backwards and forwards as far as possible and add all the matches
  (starting at same place), this should be a match too.
  also,** multiple words have same pinyin** - for the moment, this will return only the first result
  Capitals are causing issues too...
*/
public class ChineseDictionaryExtractor implements DictionaryExtractor {
    private static final String DEFAULT_DICTIONARY_FILENAME = "cedict_ts.u8";
    private static final char COMMENT_CHARACTER = '#';

    private Map<String, Word> simplifiedMapping = new HashMap<>();
    private Map<String, Word> traditionalMapping = new HashMap<>();

    @Override
    public void readInDictionary() throws URISyntaxException {
        URI defaultDictionaryPath = this.getClass().getResource("/dictionary/" + DEFAULT_DICTIONARY_FILENAME).toURI();
        readInDictionary(Path.of(defaultDictionaryPath));
    }

    @Override
    public Word getWord(char c) {
        return getWord(String.valueOf(c));
    }

    @Override
    public Word getWord(String s) {
        var word = simplifiedMapping.getOrDefault(s, traditionalMapping.get(s));
        if (Objects.nonNull(word))
            return word;

        if (mightBeErhua(s)) {
            var stripped = sanitiseErhua(s);
            return simplifiedMapping.getOrDefault(stripped, traditionalMapping.get(stripped));
        }

        return null;
    }

    public void readInDictionary(Path path) {
        try {
            Files.readAllLines(path, StandardCharsets.UTF_8).stream()
                    .filter(line -> line.charAt(0) != COMMENT_CHARACTER)
                    .map(this::getWordFromLine)
                    .forEach(this::putWordToMaps);
        } catch (IOException e) {
            System.out.println("Could not load dictionary file at " + path);
        }
    }

    private Word getWordFromLine(String line) {
        String[] str = line.split(" /");
        String definition = str[1];

        String[] rem = str[0].split("\\[");
        String pinyinNoTones = rem[1].replaceAll("[\\[\\]12345 ]", "").toLowerCase();
        String pinyinWithTones = rem[1].replaceAll("[\\[\\]]", "").toLowerCase();


        String[] remRem = rem[0].split(" ");
        String trad = remRem[0];
        String simp = remRem[1];

        return new ChineseWord(trad, simp, pinyinNoTones, pinyinWithTones, definition);
    }

    private void putWordToMaps(Word word) { // helper function for tidy stream
        if (word instanceof ChineseWord w) {
            simplifiedMapping.put(w.simplified(), word);
            traditionalMapping.put(w.traditional(), word);
        }
    }



    private boolean mightBeErhua(String word) {
        return word.lastIndexOf("儿") == word.length() - 1;
    }

    private String sanitiseErhua(String word) {
        return word.substring(0, word.lastIndexOf("儿"));
    }

    public Word getWordFromTraditionalChinese(String chineseWord) {
        return traditionalMapping.get(chineseWord);
    }

    public Word getWordFromSimplifiedChinese(String chineseWord) {
        return simplifiedMapping.get(chineseWord);
    }


}
