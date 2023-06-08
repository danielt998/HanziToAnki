package hanziToAnki.chinese;

import hanziToAnki.DictionaryExtractor;

import hanziToAnki.Word;

import java.util.*;

public class ChineseWordFinder {

    //using ABC as an example
    public enum STRATEGY {
        TRI_BI_MONOGRAMS_USE_ALL_CHARS_BIGRAM_OVERLAP(0), //default - ABC, AB, BC
        TRI_BI_MONOGRAMS_USE_ALL_CHARS(1), // ABC
        BIGRAM_AND_MONOGRAM_ONLY_NO_OVERLAP(2), // AB, BC
        BIGRAM_AND_MONOGRAM_ONLY_OVERLAP(3), // AB, BC, A, B, C
        SINGLE_CHAR_ONLY(4), // A, B, C
        ALL_COMBINATIONS(5); // ABC, AB, BC, A, B, C
        // TODO: consider some strategies that look at the frequency order

        private final int value;

        STRATEGY(final int givenValue) {
            value = givenValue;
        }

        public int getValue() { return value; }
    }

    private final DictionaryExtractor extractor;

    public ChineseWordFinder(DictionaryExtractor extractor) {
        this.extractor = extractor;
    }

    public Set<Word> findWords(STRATEGY strategy, List<String> lines) {
        char[] charArray = getCharsFromList(lines);

        return switch (strategy) {
            case SINGLE_CHAR_ONLY -> findTriBiMonograms(charArray, false, false, false, false);
            case TRI_BI_MONOGRAMS_USE_ALL_CHARS_BIGRAM_OVERLAP -> findTriBiMonograms(charArray, true, false, true, true);
            case TRI_BI_MONOGRAMS_USE_ALL_CHARS -> findTriBiMonograms(charArray, false, false, true, true);
            case ALL_COMBINATIONS -> findTriBiMonograms(charArray, true, true, true, true);
            case BIGRAM_AND_MONOGRAM_ONLY_OVERLAP -> findTriBiMonograms(charArray, false, true, true, false);
            case BIGRAM_AND_MONOGRAM_ONLY_NO_OVERLAP -> findTriBiMonograms(charArray, false, false, true, false);
            default -> throw new RuntimeException("fail");
        };
    }

    public Set<Word> findMonograms(List<String> lines) {
        char[] charArray = getCharsFromList(lines);
        return findTriBiMonograms(charArray, false, false, false, false);
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

    private Set<Word> findTriBiMonograms(char[] charArray, boolean bigramOverlap, boolean monogramOverlap, boolean includeBigrams, boolean
            includeTrigrams) {
        Set<Word> words = new LinkedHashSet<>(); // TODO: Add a test to confirm that order is preserved (might fail with a normal HashSet)
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
