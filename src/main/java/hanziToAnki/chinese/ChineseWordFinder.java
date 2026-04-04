package hanziToAnki.chinese;

import hanziToAnki.DictionaryExtractor;

import hanziToAnki.Word;

import java.util.*;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.ansj.domain.Term;

public class ChineseWordFinder {

    //using ABC as an example
    public enum STRATEGY {
        TRI_BI_MONOGRAMS_USE_ALL_CHARS_BIGRAM_OVERLAP(0), //default - ABC, AB, BC
        TRI_BI_MONOGRAMS_USE_ALL_CHARS(1), // ABC
        BIGRAM_AND_MONOGRAM_ONLY_NO_OVERLAP(2), // AB, BC
        BIGRAM_AND_MONOGRAM_ONLY_OVERLAP(3), // AB, BC, A, B, C
        SINGLE_CHAR_ONLY(4), // A, B, C
        ALL_COMBINATIONS(5), // ABC, AB, BC, A, B, C
        ANSJ_SEGMENTATION(6); // Use ANSJ library for word segmentation
        // TODO: consider some strategies that look at the frequency order

        private final int strategyIndex;

        STRATEGY(final int givenValue) {
            strategyIndex = givenValue;
        }

        public static STRATEGY getStrategy(int givenStrategy) {
            for (STRATEGY strategy: STRATEGY.values()) {
                if (strategy.strategyIndex == givenStrategy){
                    return strategy;
                }
            }
            throw new IllegalArgumentException("Strategy not found");
        }
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
            case ANSJ_SEGMENTATION -> findWordsUsingAnsj(lines);
            default -> throw new RuntimeException("fail");
        };
    }

    private Set<Word> findWordsUsingAnsj(List<String> lines) {
        Set<Word> words = new LinkedHashSet<>();
        String fullText = String.join("", lines);
        
        try {
            List<Term> terms = ToAnalysis.parse(fullText).getTerms();
            for (Term term : terms) {
                String wordStr = term.getName();
                if (isChineseOnly(wordStr)) {
                    extractor.getWord(wordStr).ifPresent(words::add);
                }
            }
        } catch (NullPointerException | IllegalArgumentException e) {
            throw new RuntimeException("ANSJ segmentation failed", e);
        }
        
        return words;
    }

    private boolean isChineseOnly(String word) {
        for (char c : word.toCharArray()) {
            if (Character.UnicodeScript.of(c) != Character.UnicodeScript.HAN) {
                return false;
            }
        }
        return true;
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

    private Set<Word> findTriBiMonograms(char[] charArray, boolean bigramOverlap, boolean monogramOverlap, 
            boolean includeBigrams, boolean includeTrigrams) {
        Set<Word> words = new LinkedHashSet<>();
        List<List<Word>> wordsForChars = getWordList(charArray);

        for (List<Word> wordList: wordsForChars) {
            List<Word> trigrams = getNgrams(wordList, 3);
            List<Word> bigrams = getNgrams(wordList, 2);
            List<Word> monograms = getNgrams(wordList, 1);

            if (!trigrams.isEmpty() && includeTrigrams) {
                words.addAll(trigrams);
                if (bigramOverlap) {
                    words.addAll(bigrams);
                }
                if (monogramOverlap) {
                    words.addAll(monograms);
                }
            } else if (!bigrams.isEmpty() && includeTrigrams) {
                words.addAll(bigrams);
                if (monogramOverlap) {
                    words.addAll(monograms);
                }
            } else {
                words.addAll(monograms);
            }
        }
        return words;
    }

    private List<List<Word>> getWordList(char[] charArray) {
        List<List<Word>> wordsForChars = new ArrayList<>(charArray.length);
        for (int i = 0; i < charArray.length; i++) {
            wordsForChars.add(new ArrayList<>());
        }
        
        java.util.stream.IntStream.range(0, charArray.length).forEach(i -> {
            // Check for trigrams
            if (i + 2 < charArray.length) {
                extractor.getWord("" + charArray[i] + charArray[i + 1] + charArray[i + 2])
                        .ifPresent(wordThreeChars -> {
                            wordsForChars.get(i).add(wordThreeChars);
                            wordsForChars.get(i + 1).add(wordThreeChars);
                            wordsForChars.get(i + 2).add(wordThreeChars);
                        });
            }

            //bigrams
            if (i + 1 < charArray.length) {
                extractor.getWord("" + charArray[i] + charArray[i + 1])
                        .ifPresent(word -> {
                            wordsForChars.get(i).add(word);
                            wordsForChars.get(i+1).add(word);
                        });
            }

            //monogram
            extractor.getWord(charArray[i]).ifPresent(word -> wordsForChars.get(i).add(word));
        });
        return wordsForChars;
    }

    private char[] getCharsFromList(List<String> lines) {
        String fullString = String.join("", lines);
        return fullString.chars()
                .filter(c -> Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN)
                .mapToObj(c -> String.valueOf((char) c))
                .collect(StringBuilder::new, StringBuilder::append, StringBuilder::append)
                .toString()
                .toCharArray();
    }
}
