package hanziToAnki;

import dictionary.Word;

import java.util.List;
import java.util.Set;

public class DeckFactory {
    private static final String DELIMITER = "\t";
    private static final String CLOSING_HTML_TAG = "</span>";

    public static Deck generateDeck(Set<Word> words) {
        Deck deck = new Deck();
        for (Word word : words) {
            deck.addLine(getWordAsDeckLine(word));
        }
        return deck;
    }

    private static String getWordAsDeckLine(Word word) {
        return String.join(DELIMITER,
            word.simplified(),
            word.definition(),
            getPinyinWithHTML(word)
        );
    }

    private static String getPinyinWithHTML(Word word) {
        String pinyin = word.pinyinTones();
        String[] syllables = pinyin.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String syllable : syllables) {
            int tone = Integer.parseInt("" + syllable.charAt(syllable.length() - 1));
            builder.append(getOpeningHTMLTag(tone));
            builder.append(ToneHelper.convertNumberedSyllableToAccentedSyllable(syllable));
            builder.append(CLOSING_HTML_TAG);
        }
        return builder.toString();
    }

    private static String getOpeningHTMLTag(int tone) {
        return "<span class=\"tone" + tone + "\">";
    }
}
