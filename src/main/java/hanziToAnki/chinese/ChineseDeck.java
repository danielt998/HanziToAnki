package hanziToAnki.chinese;

import hanziToAnki.Deck;
import hanziToAnki.Word;

import java.util.List;
import java.util.Set;

public class ChineseDeck implements Deck {
    private static final String DELIMITER = "\t";
    private static final String CLOSING_HTML_TAG = "</span>";

    public List<String> generate(Set<Word> words) {
        return words.stream()
                .map(this::getWordAsDeckLine)
                .toList();
    }

    private String getWordAsDeckLine(Word word) {
        var w = (ChineseWord) word;
        return String.join(DELIMITER,
                w.simplified(),
                w.definition(),
                getPinyinWithHTML(w)
        );
    }

    private static String getPinyinWithHTML(ChineseWord word) {
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