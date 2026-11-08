package chinese;

import static org.junit.jupiter.api.Assertions.assertEquals;

import hanziToAnki.chinese.ToneHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ToneHelperTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        // Single vowels across tones 1-4
        "kan1, kān",
        "He2, Hé",
        "pin3, pǐn",
        "po4, pò",
        "lü1, lǖ",
        "nü3, nǚ",

        // Compound vowels: 'iu' rule (tone mark on 'u')
        "liu2, liú",
        "jiu3, jiǔ",
        "qiu1, qiū",

        // Compound vowels: 'ui' rule (tone mark on 'i')
        "gui4, guì",
        "dui1, duī",
        "shui3, shuǐ",

        // Vowel precedence ('a', 'o', 'e' take priority)
        "xiao3, xiǎo",
        "kuai4, kuài",
        "tian1, tiān",
        "gou3, gǒu",
        "duo1, duō",
        "lei3, lěi",
        "she4, shè",

        // Colon-based umlaut notation ('u:' -> 'ü')
        "lu:3, lǚ",
        "Nu:3, Nǚ",
        "qu:4, qǜ",

        // Neutral tone (tone 5 leaves character untoned and strips digit)
        "Wu5, Wu",
        "ba5, ba",
        "de5, de",
        "ma5, ma"
    })
    @DisplayName("Convert numbered pinyin syllable to accented syllable")
    void convertNumberToAccentedSyllable(String inputSyllable, String expectedOutput) {
        String result = ToneHelper.convertNumberedSyllableToAccentedSyllable(inputSyllable);
        assertEquals(expectedOutput, result);
    }
}
