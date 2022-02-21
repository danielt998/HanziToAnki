package chinese;

import hanziToAnki.chinese.ToneHelper;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

class ToneHelperTest {
    @ParameterizedTest
    @CsvSource({
            "kan1,kān",
            "He2,Hé",
            "pin3,pǐn",
            "po4,pò",
            "Wu5,Wu",
            "lü1,lǖ"
    })
    void convertNumberToAccentedSyllable(String inputSyllable, String expectedOutput) {
        String result = ToneHelper.convertNumberedSyllableToAccentedSyllable(inputSyllable);
        assertEquals(expectedOutput, result);
    }

}