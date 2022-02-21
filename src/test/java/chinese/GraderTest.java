package chinese;

import hanziToAnki.DictionaryExtractor;
import hanziToAnki.Grader;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import hanziToAnki.chinese.ChineseGrader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GraderTest {
    private static Grader grader;

    @BeforeAll
    public static void setup() throws URISyntaxException {
        DictionaryExtractor extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();
        grader = new ChineseGrader(extractor);
    }

    @Test
    void getAccumulativeHSKVocabularyTest() {
        // Note - accumulative counts are 352, 654, 1254, 2554, and 5054
        // The difference is presumably due to some HSK words not being in dictionary
        assertEquals(149, grader.getAccumulativeVocabulary(1).size());
        assertEquals(297, grader.getAccumulativeVocabulary(2).size());
        assertEquals(593, grader.getAccumulativeVocabulary(3).size());
        assertEquals(1188, grader.getAccumulativeVocabulary(4).size());
        assertEquals(2486, grader.getAccumulativeVocabulary(5).size());
        assertEquals(4983, grader.getAccumulativeVocabulary(6).size());
    }

    // TODO parameterised test that checks we get the correct HSK levels (1,2,4,5,6) for 6 words
}