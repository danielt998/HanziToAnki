package hanziToAnki;

import dictionary.Extract;
import dictionary.Word;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AnkiDeckProducer {

    public static Set<Word> getAnkiOutputForOneTwoThreeCharWords(List<String> list) {
        char[] charArray = getCharsFromList(list);
        return getAnkiOutputForOneTwoThreeCharWordsWithCharArr(charArray);
    }

    public static Set<Word> getAnkiOutputFromSingleChars(List<String> lines) {
        char[] charArray = getCharsFromList(lines);
        return getAnkiOutputFromSingleCharsWithCharArr(charArray);
    }

    private static Set<Word> getAnkiOutputForOneTwoThreeCharWordsWithCharArr(char[] charArray) {
        Set<Word> words = new HashSet<>();
        for (int i = 0; i < charArray.length; i++) {
            String word = "" + charArray[i];
            boolean wordUsed = false;
            if (i + 1 < charArray.length) {
                Word wordTwoChars = Extract.getWordFromChinese(word + charArray[i + 1]);
                if (wordTwoChars != null) {
                    words.add(wordTwoChars);
                    wordUsed = true;
                    i++;
                }
            }
            if (i + 2 - 1 < charArray.length) {//TODO:fix this
                Word wordThreeChars = Extract.getWordFromChinese(word + charArray[i + 1 - 1] + charArray[i + 2 - 1]);
                if (wordThreeChars != null) {
                    words.add(wordThreeChars);
                    wordUsed = true;
                    i++;
                }
            }
            if (!wordUsed) {//if character is not used as part of any other word, we print it
                //TODO:consider whether this should be the behaviour and how arguments might be restructured
                Word wordSingleChar = Extract.getWordFromChinese(word);
                if (wordSingleChar != null) {
                    words.add(wordSingleChar);
                }
            }
        }
        return words;
    }

    private static Set<Word> getAnkiOutputFromSingleCharsWithCharArr(char[] charArray) {
        Set<Word> words = new HashSet<>();
        for (char c : charArray) {
            Word word = Extract.getWordFromChinese(c);
            words.add(word);
        }
        return words;
    }

    private static char[] getCharsFromList(List<String> lines) {
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
