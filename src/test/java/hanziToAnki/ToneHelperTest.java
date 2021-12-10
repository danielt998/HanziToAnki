package hanziToAnki;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ToneHelperTest {

    @ParameterizedTest
    @CsvSource({"a,1,ā",
                "e,2,é",
                "i,3,ǐ",
                "o,4,ò",
                "u,5,u",
                "ü,1,ǖ"})
    void getCharWithTone(Character input, int tone, Character expected) {
        char result = ToneHelper.getCharWithTone(input, tone);
        assertEquals(expected, result);
    }

//    @Test
//    void getLetterForTone() {
//        fail("Not yet implemented");
//    }
}