import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class VocabularyImporterTest {

    @BeforeEach
    void setUp() throws URISyntaxException {
        Extract.readInDictionary(); // We need to load in Words so we can e.g. find Words of HSK1+2 entries
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void getAccumulativeHSKVocabularyTest() {
        // Note - accumulative counts are 352, 654, 1254, 2554, and 5054
        // The difference is presumably due to some HSK words not being in dictionary
        assertEquals(149, VocabularyImporter.getAccumulativeHSKVocabulary(1).size());
        assertEquals(297, VocabularyImporter.getAccumulativeHSKVocabulary(2).size());
        assertEquals(593, VocabularyImporter.getAccumulativeHSKVocabulary(3).size());
        assertEquals(1188, VocabularyImporter.getAccumulativeHSKVocabulary(4).size());
        assertEquals(2486, VocabularyImporter.getAccumulativeHSKVocabulary(5).size());
        assertEquals(4982, VocabularyImporter.getAccumulativeHSKVocabulary(6).size());
    }

    @Test
    void getWordsFromStringList() {
    }
}