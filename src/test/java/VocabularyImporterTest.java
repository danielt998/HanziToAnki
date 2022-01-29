import hanziToAnki.DictionaryExtractor;
import hanziToAnki.Grader;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import hanziToAnki.chinese.ChineseGrader;
import hanziToAnki.chinese.ChineseWord;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class VocabularyImporterTest {

    DictionaryExtractor extractor = new ChineseDictionaryExtractor();
    @BeforeEach
    void setUp() throws URISyntaxException {
        extractor.readInDictionary(); // We need to load in Words so we can e.g. find Words of HSK1+2 entries
    }

    @Test
    void getAccumulativeHSKVocabularyTest() {
        // Note - accumulative counts are 352, 654, 1254, 2554, and 5054
        // The difference is presumably due to some HSK words not being in dictionary
        Grader grader = new ChineseGrader(null); // TODO pass in a dictionary extractor
        assertEquals(149, grader.getAccumulativeVocabulary(1).size());
        assertEquals(297, grader.getAccumulativeVocabulary(2).size());
        assertEquals(593, grader.getAccumulativeVocabulary(3).size());
        assertEquals(1188, grader.getAccumulativeVocabulary(4).size());
        assertEquals(2486, grader.getAccumulativeVocabulary(5).size());
        assertEquals(4983, grader.getAccumulativeVocabulary(6).size());
    }

    @Test
    void getWordsFromStringListTest() {
        List<String> hskMixed = List.of("爱", "吧", "阿姨");

        Grader grader = new ChineseGrader(null); // TODO pass in a dictionary extractor

        // Collect Set to sorted List so order is always guaranteed
        List<ChineseWord> hskWords = grader.noGrading(hskMixed).stream()
                .sorted(Comparator.comparing(Object::toString))
                .map(w -> (ChineseWord) w)
                .collect(Collectors.toList());

        assertEquals("ai", hskWords.get(0).pinyin());
        assertEquals("ayi", hskWords.get(1).pinyin());
        assertEquals("ba", hskWords.get(2).pinyin());
    }
}