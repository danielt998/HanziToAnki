import static org.junit.jupiter.api.Assertions.assertTrue;

import hanziToAnki.DictionaryExtractor;
import hanziToAnki.VocabularyScriptRunner;
import hanziToAnki.Word;
import hanziToAnki.chinese.ChineseWord;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VocabularyScriptRunnerTest {

    @Test
    void runsAssignmentsAndWritesDecks(@TempDir Path temporaryDirectory) throws Exception {
        Path input = temporaryDirectory.resolve("input.tsv");
        Path output = temporaryDirectory.resolve("output.tsv");
        Path script = temporaryDirectory.resolve("plan.h2a");
        Files.writeString(input, "word\n");
        Files.writeString(script, """
                cards = open_cards("%s")
                write_cards(cards, "%s")
                """.formatted(input, output));

        new VocabularyScriptRunner(extractor()).runScript(script);

        assertTrue(Files.readString(output).contains("word"));
    }

    private static DictionaryExtractor extractor() {
        Word word = new ChineseWord("word", "word", "word", "word1", "definition");
        return new DictionaryExtractor() {
            @Override
            public void readInDictionary() {
            }

            @Override
            public Optional<Word> getWord(String value) {
                return value.equals("word") ? Optional.of(word) : Optional.empty();
            }

            @Override
            public Optional<Word> getWord(char value) {
                return Optional.empty();
            }
        };
    }
}
