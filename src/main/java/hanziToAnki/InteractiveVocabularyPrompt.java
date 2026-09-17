package hanziToAnki;

import hanziToAnki.chinese.ChineseDictionaryExtractor;
import java.io.IOException;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public final class InteractiveVocabularyPrompt {
    private InteractiveVocabularyPrompt() {
    }

    public static void run() throws IOException {
        DictionaryExtractor extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();
        VocabularyScriptRunner runner = new VocabularyScriptRunner(extractor);

        try (Terminal terminal = TerminalBuilder.builder().system(true).build()) {
            LineReader input = LineReaderBuilder.builder().terminal(terminal).build();
            System.out.println("Vocabulary shell. Type help for commands or exit to quit.");
            while (true) {
                String statement;
                try {
                    statement = input.readLine("vocab> ").trim();
                } catch (UserInterruptException ignored) {
                    continue;
                }
                if (statement.equalsIgnoreCase("exit") || statement.equalsIgnoreCase("quit")) {
                    return;
                }
                if (statement.equalsIgnoreCase("help")) {
                    printHelp();
                    continue;
                }
                if (statement.equalsIgnoreCase("variables")) {
                    System.out.println(runner.variableNames());
                    continue;
                }
                try {
                    String result = runner.execute(statement);
                    if (!result.isEmpty()) {
                        System.out.println(result);
                    }
                } catch (IllegalArgumentException exception) {
                    System.out.println(exception.getMessage());
                }
            }
        } catch (EndOfFileException ignored) {
            System.out.println("Interactive vocabulary builder cancelled.");
        }
    }

    private static void printHelp() {
        System.out.println("variable = expression");
        System.out.println("write_cards(expression, \"output.tsv\")");
        System.out.println("Functions: old_hsk(\"5\"), old_hsk(\"1-5\"), old_hsk(\"1,3,5\"), open_cards(\"input.tsv\")");
        System.out.println("Operators: +, -, &, and parentheses. Commands: variables, help, exit.");
    }

}
