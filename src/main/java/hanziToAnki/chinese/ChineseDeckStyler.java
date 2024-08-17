package hanziToAnki.chinese;

import hanziToAnki.DeckStyler;
import hanziToAnki.Word;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChineseDeckStyler implements DeckStyler {
    private static final String DELIMITER = "\t";
    private static final String CLOSING_HTML_TAG = "</span>";
    private static final List<String> HEADERS = Arrays.asList(
            "#notetype:Chinese",
            "Hanzi	English	Pinyin"
    );


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
        List<String> wordList = words.stream()
                .map(this::getWordAsDeckLine)
                .toList();
        return Stream.of(HEADERS, wordList).flatMap(Collection::stream).collect(Collectors.toList());
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
