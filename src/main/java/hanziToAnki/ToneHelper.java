package hanziToAnki;

public class ToneHelper {

    private static final char[] VOWELS_UNACCENTED = new char[]{'a', 'e', 'i', 'o', 'u', 'ü'};
    private static final char[] A_ACCENTs = new char[]{'ā', 'á', 'ǎ', 'à', 'a'};
    private static final char[] E_ACCENTs = new char[]{'ē', 'é', 'ě', 'è', 'e'};
    private static final char[] I_ACCENTs = new char[]{'ī', 'í', 'ǐ', 'ì', 'i'};
    private static final char[] O_ACCENTs = new char[]{'ō', 'ó', 'ǒ', 'ò', 'o'};
    private static final char[] U_ACCENTs = new char[]{'ū', 'ú', 'ǔ', 'ù', 'u'};
    private static final char[] U_UMLAUT_ACCENTs = new char[]{'ǖ', 'ǘ', 'ǚ', 'ǜ', 'ü'};

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

        char c = switch (Character.toLowerCase(originalChar)) {
            case 'a' -> A_ACCENTs[tone - 1];
            case 'e' -> E_ACCENTs[tone - 1];
            case 'i' -> I_ACCENTs[tone - 1];
            case 'o' -> O_ACCENTs[tone - 1];
            case 'u' -> U_ACCENTs[tone - 1];
            case 'ü' -> U_UMLAUT_ACCENTs[tone - 1];
            default -> Character.toLowerCase(originalChar);
        };

        if (Character.isLowerCase(originalChar))
            return c;
        return Character.toUpperCase(c);
    }
    
    private static boolean isCharTonable(char testChar) {
        for (char c : VOWELS_UNACCENTED) {
            if (c == testChar)
                return true;
        }
        return false;
    }

    public static String getPinyinWithMarks(String syllable) {
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
