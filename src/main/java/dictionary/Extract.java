package dictionary;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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
public class Extract {
    private static final String DEFAULT_DICTIONARY_FILENAME = "cedict_ts.u8";
    private static final char COMMENT_CHARACTER = '#';
    private static final Map<String, Word> simplifiedMapping = new HashMap<>();
    private static final Map<String, Word> traditionalMapping = new HashMap<>();

    public static void readInDictionary() throws URISyntaxException {
        URI defaultDictionaryPath = Extract.class.getResource(DEFAULT_DICTIONARY_FILENAME).toURI();
        readInDictionary(Path.of(defaultDictionaryPath));
    }

    public static void readInDictionary(Path path) {
        try {
            Files.readAllLines(path).stream()
                    .filter(line -> line.charAt(0) != COMMENT_CHARACTER)
                    .map(Extract::getWordFromLine)
                    .forEach(Extract::putWordToMaps);
        } catch (IOException e) {
            System.out.println("Could not load dictionary file at " + path);
        }
    }

    private static Word getWordFromLine(String line) {
        String[] str = line.split(" /");
        String definition = str[1];

        String[] rem = str[0].split("\\[");
        String pinyinNoTones = rem[1].replaceAll("[\\[\\]12345 ]", "").toLowerCase();
        String pinyinWithTones = rem[1].replaceAll("[\\[\\]]", "").toLowerCase();



        String[] remRem = rem[0].split(" ");
        String trad = remRem[0];
        String simp = remRem[1];

        return new Word(trad, simp, pinyinNoTones, pinyinWithTones, definition);
    }

    private static void putWordToMaps(Word word) { // helper function for tidy stream
        simplifiedMapping.put(word.simplified(), word);
        traditionalMapping.put(word.traditional(), word);
    }

    public static Word getWordFromChinese(char c) {
        return getWordFromChinese(String.valueOf(c));
    }

    public static Word getWordFromChinese(String chineseWord) {
        if (simplifiedMapping.containsKey(chineseWord))
            return simplifiedMapping.get(chineseWord);
        return traditionalMapping.get(chineseWord);
    }

    public static Word getWordFromTraditionalChinese(String chineseWord) {
        return traditionalMapping.get(chineseWord);
    }

    public static Word getWordFromSimplifiedChinese(String chineseWord) {
        return simplifiedMapping.get(chineseWord);
    }

/*TODO:resurrect
  //LINEAR COMPLEXITY
  public static String getEnglish(String chineseWord){
    for (dictHandler.Word word : dictionary){
      if(word.getSimplifiedChinese().equals(chineseWord)
                      || word.getTraditionalChinese().equals(chineseWord)){
        return word.getDefinition();
      }
    }
    return "Chinese word not found";
  }
  public static String getPinyinWithTones(String chineseWord){
    for (dictHandler.Word word : dictionary){
      if(word.getSimplifiedChinese().equals(chineseWord)
                      || word.getTraditionalChinese().equals(chineseWord)){
        return word.getPinyinWithTones();
      }
    }
    return "Chinese word not found";
  }
  */

}
