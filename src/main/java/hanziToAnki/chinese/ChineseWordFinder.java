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
            case ALL_COMBINATIONS_TWO_OR_MORE -> findAllCombinationsTwoOrMore(lines, false);
            case ALL_COMBINATIONS_TWO_OR_MORE_FALLBACK_TO_SINGLE -> findAllCombinationsTwoOrMore(lines, true);
            case ALL_COMBINATIONS -> findAllCombinations(lines);
            case LONGEST_WORDS_ONLY -> findLongestWords(lines);
            default -> throw new RuntimeException("fail");
        };
    }

    private Set<Word> findLongestWords(List<String> lines) {
        char[] charArray = getCharsFromList(lines);
        return findLongestWords(charArray);
    }

    private Set<Word> findLongestWords(char[] charArray) {
        Set<Word> allWords = new HashSet<Word>();
        for (int i = 0; i < charArray.length - 2; i++) {
            for (int n = 3; n > 0; n--) {//assuming start at 3 TODO:make even more generic?
                Set<Word> nGrams =  findNGrams(charArray, i, 3, n);
                if (!nGrams.isEmpty()){
                    allWords.addAll(nGrams);
                    break;
                }
            }
        }
        return allWords;
    }

    public Set<Word> findMonograms(List<String> lines) {
        char[] charArray = getCharsFromList(lines);
        return findNGrams(charArray, 0, charArray.length, 1);
    }

    private Set<Word> findAllCombinations(List<String> lines) {
        char[] charArray = getCharsFromList(lines);
        return findAllCombinations(charArray);
    }

    private Set<Word> findAllCombinationsTwoOrMore(List<String> lines, boolean fallbackToSingle) {
        char[] charArray = getCharsFromList(lines);
        return findAllCombinationsTwoOrMore(charArray, fallbackToSingle);
    }

    private Set<Word> findAllCombinations(char[] charArray) {
        Set<Word> allWords = new HashSet<Word>();
        for (int i = 0; i < charArray.length - 2; i++) {
            Set<Word> bigrams = findNGrams(charArray, i, 3, 2);
            Set<Word> trigrams = findNGrams(charArray, i, 3, 3);
            Set<Word> monograms = findNGrams(charArray, i, 3, 1);
            allWords.addAll(bigrams);
            allWords.addAll(trigrams);
            allWords.addAll(monograms);
        }
        return allWords;
    }

    private Set<Word> findAllCombinationsTwoOrMore(char[] charArray, boolean fallbackToSingle) {
        Set<Word> allWords = new HashSet<Word>();
        for (int i = 0; i < charArray.length - 2; i++) {
            Set<Word> bigrams = findNGrams(charArray, i, 3, 2);
            Set<Word> trigrams = findNGrams(charArray, i, 3, 3);
            allWords.addAll(bigrams);
            allWords.addAll(trigrams);
            if (fallbackToSingle && allWords.isEmpty()) {
                allWords.addAll(findNGrams(charArray, i, 3, 1));
            }
        }
        return allWords;
    }

    private Set<Word> findNGrams(char[] charArray, int startIndex, int searchWindowLength, int n) {
        Set<Word> words = new HashSet<Word>();
        for (int i = 0; i < searchWindowLength - (n - 1); i++) {
            String nGramString = "";
            for (int j = 0; j < n; j++) {
                nGramString = nGramString + charArray[startIndex + i + j];
            }

            Word nGram = extractor.getWord(nGramString);
            if (nGram != null) {
                words.add(nGram);
            }
        }
        return words;
    }

    public Set<Word> findMonoBiTriGrams(List<String> list) {
        char[] charArray = getCharsFromList(list);
        return findMonoBiTriGrams(charArray);
    }


    //TODO: delete this and replace with shiny new methods
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
