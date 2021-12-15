package hanziToAnki.decks;

import dictionary.Word;
import hanziToAnki.ToneHelper;
import hanziToAnki.decks.Deck;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;


public class ChineseDeck implements Deck {
    private static final String DELIMITER = "\t";
    private static final String CLOSING_HTML_TAG = "</span>";

    private List<String> lines;

    public ChineseDeck() {}

    @Override
    public void generate(Set<Word> words) {
        this.lines = words.stream()
                .map(ChineseDeck::getWordAsDeckLine)
                .collect(toList());
    }

    public List<String> getLines() {
        return lines;
    }

    private static String getWordAsDeckLine(Word word) {
        return word.simplified() +
                DELIMITER + word.definition() +
                DELIMITER + getPinyinWithHTML(word) +
                DELIMITER + getSimpWithToneInfo(word);
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

    private static String getSimpWithToneInfo(Word word) {
        return "";
    }
}
