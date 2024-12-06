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
            "#notetype:Chinese"
    );

    private final HanziType hanziType;

    public enum HanziType {
        SIMP(0),
        TRAD(1),
        SIMP_AND_TRAD(2);

        private final int hanziTypeIndex;

        HanziType(final int givenValue) {
            hanziTypeIndex = givenValue;
        }

        public static HanziType getHanziType(int givenHanziType) {
            for (HanziType hanziType: HanziType.values()) {
                if (hanziType.hanziTypeIndex == givenHanziType){
                    return hanziType;
                }
            }
            throw new IllegalArgumentException("Hanzi type not found");
        }
    }

    public ChineseDeckStyler(HanziType givenHanziType) {
        this.hanziType = givenHanziType;
    }

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
        //TODO: consider a different notetype if including both trad and simp
        return Stream.of(HEADERS, wordList).flatMap(Collection::stream).collect(Collectors.toList());
    }

    private String getWordAsDeckLine(Word word) {
        var w = (ChineseWord) word;
        return switch (hanziType) {
            case SIMP -> String.join(DELIMITER,
                    w.simplified(),
                    w.definition(),
                    getPinyinWithHtml(w)
            );
            case TRAD -> String.join(DELIMITER,
                    w.traditional(),
                    w.definition(),
                    getPinyinWithHtml(w)
            );
            case SIMP_AND_TRAD -> String.join(DELIMITER,
                    w.simplified(),
                    w.traditional(),
                    w.definition(),
                    getPinyinWithHtml(w)
            );
        };
    }
}
