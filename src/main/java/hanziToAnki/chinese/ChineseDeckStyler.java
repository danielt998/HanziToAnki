package hanziToAnki.chinese;

import hanziToAnki.DeckStyler;
import hanziToAnki.Word;
import java.util.List;
import java.util.Set;

public class ChineseDeckStyler implements DeckStyler {
    private static final String DELIMITER = "\t";
    private static final String CLOSING_HTML_TAG = "</span>";

    private static String getPinyinWithHtml(ChineseWord word) {
        String pinyin = word.pinyinTones();
        String[] syllables = pinyin.split(" ");
        StringBuilder builder = new StringBuilder();
        for (String syllable : syllables) {
            int tone = Integer.parseInt("" + syllable.charAt(syllable.length() - 1));
            builder.append(getOpeningHtmlTag(tone));
            builder.append(ToneHelper.convertNumberedSyllableToAccentedSyllable(syllable));
            builder.append(CLOSING_HTML_TAG);
        }
        return builder.toString();
    }

    private static String getOpeningHtmlTag(int tone) {
        return "<span class=\"tone" + tone + "\">";
    }

    public List<String> style(Set<Word> words) {
        return words.stream()
                .map(this::getWordAsDeckLine)
                .toList();
    }

    private String getWordAsDeckLine(Word word) {
        var w = (ChineseWord) word;
        return String.join(DELIMITER,
                w.simplified(),
                w.definition(),
                getPinyinWithHtml(w)
        );
    }
}
