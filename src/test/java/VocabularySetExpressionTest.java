import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import hanziToAnki.DictionaryExtractor;
import hanziToAnki.VocabularySetExpression;
import hanziToAnki.VocabularySetOperations;
import hanziToAnki.Word;
import hanziToAnki.chinese.ChineseWord;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;

class VocabularySetExpressionTest {

    @Test
    void supportsNestedParenthesesAndOperatorPrecedence() {
        ChineseWord one = word("一");
        ChineseWord two = word("二");
        ChineseWord three = word("三");
        VocabularySetOperations operations = new VocabularySetOperations(emptyExtractor());
        Map<String, Set<Word>> sources = Map.of(
                "open_cards:book", new LinkedHashSet<>(Set.of(one, two, three)),
                "old_hsk:5", new LinkedHashSet<>(Set.of(two)),
                "open_cards:known", new LinkedHashSet<>(Set.of(three)));

        var result = new VocabularySetExpression(
                "open_cards(\"book\")-(old_hsk(\"5\") + open_cards(\"known\"))",
                (functionName, argument) -> sources.get(functionName + ":" + argument),
                operations).evaluate();

        assertEquals(Set.of(one), result);
    }

    @Test
    void rejectsUnclosedParentheses() {
        VocabularySetOperations operations = new VocabularySetOperations(emptyExtractor());

        assertThrows(IllegalArgumentException.class, () ->
                new VocabularySetExpression(
                        "open_cards(\"book\") - (open_cards(\"known\")",
                        (functionName, argument) -> Set.of(),
                        operations).evaluate());
    }

    private static ChineseWord word(String value) {
        return new ChineseWord(value, value, value, value + "1", value);
    }

    private static DictionaryExtractor emptyExtractor() {
        return new DictionaryExtractor() {
            @Override
            public void readInDictionary() {
            }

            @Override
            public Optional<Word> getWord(String value) {
                return Optional.empty();
            }

            @Override
            public Optional<Word> getWord(char value) {
                return Optional.empty();
            }
        };
    }
}
