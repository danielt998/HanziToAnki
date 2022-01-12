package hanziToAnki;

import dictionary.Word;

import java.util.List;
import java.util.Set;

public class ChineseDeckFactory implements DeckFactory {
    private static final String DELIMITER = "\t";
    private static final String CLOSING_HTML_TAG = "</span>";

    public List<String> generateDeck(Set<Word> words) {
        return words.stream()
                .map(this::getWordAsDeckLine)
                .toList();
    }

    private String getWordAsDeckLine(Word word) {
        return String.join(DELIMITER,
            word.simplified(),
            word.definition(),
            getPinyinWithHTML(word)
        );
    }

    private String getPinyinWithHTML(Word word) {
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

    private String getOpeningHTMLTag(int tone) {
        return "<span class=\"tone" + tone + "\">";
    }
}
