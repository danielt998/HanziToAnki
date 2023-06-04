package hanziToAnki.chinese;

import hanziToAnki.DictionaryExtractor;

import hanziToAnki.Word;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class ChineseWordFinder {

    //using ABC as an example
    public enum Strategy {
        TRI_BI_MONOGRAMS_USE_ALL_CHARS_BIGRAM_OVERLAP, //default - ABC, AB, BC
        TRI_BI_MONOGRAMS_USE_ALL_CHARS, // ABC
        BIGRAM_AND_MONOGRAM_ONLY_NO_OVERLAP, // AB, BC
        BIGRAM_AND_MONOGRAM_ONLY_OVERLAP, // AB, BC, A, B, C
        SINGLE_CHAR_ONLY, // A, B, C
        ALL_COMBINATIONS, // ABC, AB, BC, A, B, C
        // Note the below strategies will sometimes miss characters
        ALL_COMBINATIONS_TWO_OR_MORE, // ABC, AB, BC - but consider no trigram and single digram, one char will get missed
        ALL_COMBINATIONS_TWO_OR_MORE_FALLBACK_TO_SINGLE, // As above, but if no matches, fall back to A, B, C
        LONGEST_WORDS_ONLY, // ABC, if ABC not valid, AB and BC, is those not valid, A, B, C
        // TODO: consider some strategies that look at the frequency order
    }

    private final DictionaryExtractor extractor;

    public ChineseWordFinder(DictionaryExtractor extractor) {
        this.extractor = extractor;
    }

    public Set<Word> findWords(Strategy strategy, List<String> lines) {
        char[] charArray = getCharsFromList(lines);

        return switch (strategy) {
            case SINGLE_CHAR_ONLY -> newFindTriBiMonograms(charArray, false, false, false, false);
            case TRI_BI_MONOGRAMS_USE_ALL_CHARS_BIGRAM_OVERLAP -> newFindTriBiMonograms(charArray, true, false, true, true);
            case TRI_BI_MONOGRAMS_USE_ALL_CHARS -> newFindTriBiMonograms(charArray, false, false, true, true);
            case ALL_COMBINATIONS -> newFindTriBiMonograms(charArray, true, true, true, true);
            case BIGRAM_AND_MONOGRAM_ONLY_OVERLAP -> newFindTriBiMonograms(charArray, false, true, true, false);
            case BIGRAM_AND_MONOGRAM_ONLY_NO_OVERLAP -> newFindTriBiMonograms(charArray, false, false, true, false);

            ///below ar all dumb things that don't necessarily cover all chars
            case ALL_COMBINATIONS_TWO_OR_MORE -> findAllCombinationsTwoOrMore(charArray, false);
            case ALL_COMBINATIONS_TWO_OR_MORE_FALLBACK_TO_SINGLE -> findAllCombinationsTwoOrMore(charArray, true);

            case LONGEST_WORDS_ONLY -> findLongestWords(charArray);
            default -> throw new RuntimeException("fail");
        };
    }

    private Set<Word> findLongestWords(char[] charArray) {
        Set<Word> allWords = new LinkedHashSet<Word>();
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

    private Set<Word> findAllCombinationsTwoOrMore(char[] charArray, boolean fallbackToSingle) {
        Set<Word> allWords = new LinkedHashSet<>();
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
        Set<Word> words = new LinkedHashSet<>();
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

    private List<Word> getNgrams(List<Word> wordList, int n) {
        List<Word> newWordList = new ArrayList<>();
        for (Word word: wordList) {
            if(((hanziToAnki.chinese.ChineseWord)word).simplified().length() == n) {
                newWordList.add(word);
            }
        }
        return newWordList;
    }

    private Set<Word> newFindTriBiMonograms(char[] charArray, boolean bigramOverlap, boolean monogramOverlap, boolean includeBigrams, boolean
            includeTrigrams) {
        Set<Word> words = new LinkedHashSet<>();
        List<List<Word>> wordsForChars = getWordList(charArray);

        for (List<Word> wordList: wordsForChars) {
            List<Word> trigrams = getNgrams(wordList, 3);
            List<Word> bigrams = getNgrams(wordList, 2);
            List<Word> monograms = getNgrams(wordList, 1);

            if (trigrams.size() != 0 && includeTrigrams) {
                words.addAll(trigrams);
                if (bigramOverlap) {
                    words.addAll(bigrams);
                }
                if (monogramOverlap) {
                    words.addAll(monograms);
                }
            } else if (bigrams.size() != 0 && includeTrigrams) {
                words.addAll(bigrams);
                if(monogramOverlap) {
                    words.addAll(monograms);
                }
            } else {
                words.addAll(monograms);
            }
        }
        return words;
    }

    private List<List<Word>> getWordList(char[] charArray) {
        List<List<Word>> wordsForChars = new ArrayList<List<Word>>(charArray.length);
        for (int i = 0; i < charArray.length; i++) {
            wordsForChars.add(new ArrayList<>());
        }
        for (int i = 0; i < charArray.length; i++) {
            //TODO:genericise
            //trigrams
            if (i + 2 < charArray.length) {
                Word wordThreeChars = (Word) extractor.getWord("" + charArray[i] + charArray[i + 1] + charArray[i + 2]);
                if (wordThreeChars != null) {
                    wordsForChars.get(i).add(wordThreeChars);
                    wordsForChars.get(i + 1).add(wordThreeChars);
                    wordsForChars.get(i + 2).add(wordThreeChars);
                }
            }

            //bigrams
            if (i + 1 < charArray.length) {
                Word word = extractor.getWord("" + charArray[i] + charArray[i + 1]);
                if (word != null) {
                    wordsForChars.get(i).add(word);
                    wordsForChars.get(i+1).add(word);
                }
            }

            //monogram
            wordsForChars.get(i).add(extractor.getWord(charArray[i]));
        }
        return wordsForChars;
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
