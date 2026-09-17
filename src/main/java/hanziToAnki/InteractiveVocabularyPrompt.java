package hanziToAnki;

import hanziToAnki.chinese.ChineseDictionaryExtractor;
import java.util.Scanner;

public final class InteractiveVocabularyPrompt {
    private InteractiveVocabularyPrompt() {
    }

    public static void run() {
        DictionaryExtractor extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();
        VocabularyScriptRunner runner = new VocabularyScriptRunner(extractor);
        Scanner input = new Scanner(System.in);

        try {
            System.out.println("Vocabulary shell. Type help for commands or exit to quit.");
            while (true) {
                System.out.print("vocab> ");
                String statement = nextLine(input).trim();
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
        } catch (InputClosedException ignored) {
            System.out.println("Interactive vocabulary builder cancelled.");
        }
    }

    private static void printHelp() {
        System.out.println("variable = expression");
        System.out.println("write_cards(expression, \"output.tsv\")");
        System.out.println("Functions: old_hsk(\"5\"), old_hsk(\"1-5\"), old_hsk(\"1,3,5\"), open_cards(\"input.tsv\")");
        System.out.println("Operators: +, -, &, and parentheses. Commands: variables, help, exit.");
    }

    private static String nextLine(Scanner input) {
        if (!input.hasNextLine()) {
            throw new InputClosedException();
        }
        return input.nextLine();
    }

    private static final class InputClosedException extends RuntimeException {
    }
}
