import hanziToAnki.ArgParser;
import hanziToAnki.ArgParser.ParsedArgs;
import hanziToAnki.OutputFormat;
import hanziToAnki.chinese.ChineseDeckStyler;
import hanziToAnki.chinese.ChineseWordFinder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class ArgParserTest {

    @Test
    void parseArgsWithFilenameOnly() {
        String[] args = {"test.txt"};
        ParsedArgs result = ArgParser.parseArgs(args);

        Assertions.assertEquals(1, result.fileNames().size());
        Assertions.assertEquals("test.txt", result.fileNames().get(0));
        Assertions.assertEquals("test.tsv", result.outputFileName());
        Assertions.assertEquals(OutputFormat.ANKI, result.options().outputFormat());
        Assertions.assertEquals(0, result.options().hskLevelToExclude());
    }

    @Test
    void parseArgsWithOutputFilename() {
        String[] args = {"-o", "output.tsv", "input.txt"};
        ParsedArgs result = ArgParser.parseArgs(args);

        Assertions.assertEquals("output.tsv", result.outputFileName());
        Assertions.assertEquals("input.txt", result.fileNames().get(0));
    }

    @Test
    void parseArgsWithWordList() {
        String[] args = {"-w", "words.txt"};
        ParsedArgs result = ArgParser.parseArgs(args);

        Assertions.assertTrue(result.options().useWordList());
        Assertions.assertFalse(result.options().useAllWords());
    }

    @Test
    void parseArgsWithSingleCharacters() {
        String[] args = {"-s", "input.txt"};
        ParsedArgs result = ArgParser.parseArgs(args);

        Assertions.assertFalse(result.options().useWordList());
        Assertions.assertFalse(result.options().useAllWords());
    }

    @Test
    void parseArgsWithValidHskLevel() {
        String[] args = {"-hsk", "3", "input.txt"};
        ParsedArgs result = ArgParser.parseArgs(args);

        Assertions.assertEquals(3, result.options().hskLevelToExclude());
    }

    @Test
    void parseArgsWithInvalidHskLevel() {
        String[] args = {"-hsk", "10", "input.txt"};
        ParsedArgs result = ArgParser.parseArgs(args);

        Assertions.assertEquals(0, result.options().hskLevelToExclude());
    }

    @Test
    void parseArgsWithOutputFormat() {
        String[] args = {"-f", "pleco", "input.txt"};
        ParsedArgs result = ArgParser.parseArgs(args);

        Assertions.assertEquals(OutputFormat.PLECO, result.options().outputFormat());
    }

    @Test
    void parseArgsWithCharType() {
        String[] args = {"-c", "trad", "input.txt"};
        ParsedArgs result = ArgParser.parseArgs(args);

        Assertions.assertEquals(ChineseDeckStyler.HanziType.TRAD, result.options().hanziType());
    }

    @Test
    void parseArgsWithMultipleOptions() {
        String[] args = {"-w", "-hsk", "2", "-c", "both", "-o", "out.tsv", "words.txt"};
        ParsedArgs result = ArgParser.parseArgs(args);

        Assertions.assertTrue(result.options().useWordList());
        Assertions.assertEquals(2, result.options().hskLevelToExclude());
        Assertions.assertEquals(ChineseDeckStyler.HanziType.SIMP_AND_TRAD, result.options().hanziType());
        Assertions.assertEquals("out.tsv", result.outputFileName());
    }

    @Test
    void parseArgsDefaultStrategy() {
        String[] args = {"input.txt"};
        ParsedArgs result = ArgParser.parseArgs(args);

        Assertions.assertEquals(
            ChineseWordFinder.STRATEGY.TRI_BI_MONOGRAMS_USE_ALL_CHARS_BIGRAM_OVERLAP,
            result.options().strategy()
        );
    }
}
