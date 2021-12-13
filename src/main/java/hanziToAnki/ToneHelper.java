package hanziToAnki;

import java.util.HashMap;
import java.util.Map;

public class ToneHelper {

    private static final Map<Character, char[]> lettersWithToneMarks = new HashMap<>() {{
        put('a', new char[]{'ā', 'á', 'ǎ', 'à', 'a'});
        put('e', new char[]{'ē', 'é', 'ě', 'è', 'e'});
        put('i', new char[]{'ī', 'í', 'ǐ', 'ì', 'i'});
        put('o', new char[]{'ō', 'ó', 'ǒ', 'ò', 'o'});
        put('u', new char[]{'ū', 'ú', 'ǔ', 'ù', 'u'});
        put('ü', new char[]{'ǖ', 'ǘ', 'ǚ', 'ǜ', 'ü'});
    }};

    private static char getCharWithTone(char originalChar, int tone) {
        if (tone < 1 || tone > 5){
            System.err.println("An unknown tone was encountered: " + tone + "defaulting to untoned syllable");
            return originalChar;
        }
        if (!isCharTonable(Character.toLowerCase(originalChar))) {
            System.err.println("Character cannot be toned: " + originalChar);
            return originalChar;
        }
        if (tone == 5) {
            return originalChar;
        }

        char charAfter = lettersWithToneMarks.get(originalChar)[tone - 1];

        if (Character.isLowerCase(originalChar))
            return charAfter;
        return Character.toUpperCase(charAfter);
    }
    
    private static boolean isCharTonable(char testChar) {
        return lettersWithToneMarks.get(testChar) != null;
    }

    public static String convertNumberedSyllableToAccentedSyllable(String syllable) {
        syllable = syllable.replace("u:", "ü").replace("U:", "Ü");

        int tone = Integer.parseInt("" + syllable.charAt(syllable.length() - 1));
        if (syllable.contains("iu")) {
            return syllable.replace('u', getCharWithTone('u', tone)).substring(0, syllable.length() - 1);
        }
        else for (char vowel: "AaOoEeIiUuÜü".toCharArray()) {
            if (syllable.contains("" + vowel)) {
                return syllable.replace(vowel, getCharWithTone(vowel, tone)).substring(0, syllable.length() - 1);
            }
        }
        return syllable;
    }
}
