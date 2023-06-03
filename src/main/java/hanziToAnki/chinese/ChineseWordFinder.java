package hanziToAnki.chinese;

import hanziToAnki.DictionaryExtractor;
import hanziToAnki.Word;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChineseWordFinder {

    //using ABC as an example
    public enum Strategy {
        SINGLE_CHAR_ONLY, // A, B, C
        ALL_COMBINATIONS_TWO_OR_MORE, // ABC, AB, BC
        ALL_COMBINATIONS_TWO_OR_MORE_FALLBACK_TO_SINGLE, // As above, but if no matches, fall back to A, B, C
        ALL_COMBINATIONS, // ABC, AB, BC, A, B, C
        // Should be the default?
        LONGEST_WORDS_ONLY, // ABC, if ABC not valid, AB and BC, is those not valid, A, B, C
        // TODO: consider some strategies that look at the frequency order
    }

    private final DictionaryExtractor extractor;

    public ChineseWordFinder(DictionaryExtractor extractor) {
        this.extractor = extractor;
    }

    public Set<Word> findWords(Strategy strategy, List<String> lines) {
        return switch (strategy) {
            case SINGLE_CHAR_ONLY -> findMonograms(lines);
            default ->  throw new RuntimeException("fail");
        };
    }

    public Set<Word> findMonograms(List<String> lines) {
        char[] charArray = getCharsFromList(lines);
        return findMonograms(charArray);
    }

    private Set<Word> findMonograms(char[] charArray) {
        Set<Word> words = new HashSet<>();
        for (char c : charArray) {
            Word word = extractor.getWord(c);
            words.add(word);
        }
        return words;
    }

    public Set<Word> findMonoBiTriGrams(List<String> list) {
        char[] charArray = getCharsFromList(list);
        return findMonoBiTriGrams(charArray);
    }

    private Set<Word> findMonoBiTriGrams(char[] charArray) {
        Set<Word> words = new HashSet<>();
        for (int i = 0; i < charArray.length; i++) {
            String word = "" + charArray[i];
            boolean wordUsed = false;
            if (i + 1 < charArray.length) {
                Word wordTwoChars = extractor.getWord(word + charArray[i + 1]);
                if (wordTwoChars != null) {
                    words.add(wordTwoChars);
                    wordUsed = true;
                    i++;
                }
            }
            //TODO:fix this
            if (i + 2 - 1 < charArray.length) {
                Word wordThreeChars = extractor.getWord(word + charArray[i + 1 - 1] + charArray[i + 2 - 1]);
                if (wordThreeChars != null) {
                    words.add(wordThreeChars);
                    wordUsed = true;
                    i++;
                }
            }
            //if character is not used as part of any other word, we print it
            if (!wordUsed) {
                //TODO:consider whether this should be the behaviour and how arguments might be restructured
                Word wordSingleChar = extractor.getWord(word);
                if (wordSingleChar != null) {
                    words.add(wordSingleChar);
                }
            }
        }
        return words;
    }

    private char[] getCharsFromList(List<String> lines) { // TODO refactor
        StringBuilder fullString = new StringBuilder();
        for (String line : lines) {
            fullString.append(line);
        }
        char[] allChars = fullString.toString().toCharArray();
        StringBuilder chineseCharsOnly = new StringBuilder();
        for (char c : allChars) {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                chineseCharsOnly.append(c);
            }
        }
        return chineseCharsOnly.toString().toCharArray();
    }
}
