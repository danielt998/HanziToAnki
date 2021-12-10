package hanziToAnki;

public class ToneHelper {

    private static final char[] VOWELS_UNACCENTED = new char[]{'a', 'e', 'i', 'o', 'u', 'ü'};
    private static final char[] A_ACCENTs = new char[]{'ā', 'á', 'ǎ', 'à', 'a'};
    private static final char[] E_ACCENTs = new char[]{'ē', 'é', 'ě', 'è', 'e'};
    private static final char[] I_ACCENTs = new char[]{'ī', 'í', 'ǐ', 'ì', 'i'};
    private static final char[] O_ACCENTs = new char[]{'ō', 'ó', 'ǒ', 'ò', 'o'};
    private static final char[] U_ACCENTs = new char[]{'ū', 'ú', 'ǔ', 'ù', 'u'};
    private static final char[] U_UMLAUT_ACCENTs = new char[]{'ǖ', 'ǘ', 'ǚ', 'ǜ', 'ü'};

    static char getLetterForTone(String syllable) {
        if (syllable.contains("E")) {
            return 'E';
        } else if (syllable.contains("A")) {
            return 'A';
        } else if (syllable.contains("Ou")) {
            return 'O';
        } else {
            for (int i = syllable.length() - 1; i >= 0; i--) {
                if (syllable.charAt(i) == ':') {
                    return 'ü';
                }
                if ("AEIOU".contains("" + syllable.charAt(i))) {
                    return syllable.charAt(i);
                }
            }
        }

        if (syllable.contains("e")) {
            return 'e';
        } else if (syllable.contains("a")) {
            return 'a';
        } else if (syllable.contains("ou")) {
            return 'o';
        } else {
            for (int i = syllable.length() - 1; i >= 0; i--) {
                if (syllable.charAt(i) == ':') {
                    return 'ü';
                }
                if ("aeiou".contains("" + syllable.charAt(i))) {
                    return syllable.charAt(i);
                }
            }
        }
        if (syllable.contains("r")) {
            return 'e';//TODO:for now, but later we may want to handle this differently
        }
        System.out.println("Something went wrong, syllable:" + syllable);
        return 'X'; // TODO what
    }

    static char getCharWithTone(char originalChar, int tone) {
        if (!isCharTonable(originalChar))
            return originalChar;

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

}
