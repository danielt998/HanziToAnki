package hanziToAnki;

import hanziToAnki.chinese.ChineseDeckStyler;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import hanziToAnki.chinese.ChineseWordFinder;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Scanner;
import java.util.Set;

public final class InteractiveVocabularyPrompt {
    private static final String OUTPUT_FILENAME = "vocabulary.tsv";

    private InteractiveVocabularyPrompt() {
    }

    public static void run() {
        DictionaryExtractor extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();
        VocabularySetOperations vocabulary = new VocabularySetOperations(extractor);
        Scanner input = new Scanner(System.in);

        try {
            System.out.println("Vocabulary set builder");
            System.out.println("Functions: old_hsk(\"5\"), old_hsk(\"1-5\"), old_hsk(\"1,3,5\"), and open_cards(\"path/to/list.tsv\").");
            while (true) {
                System.out.print("Set expression: ");
                String expression = nextLine(input).trim();
                try {
                    LinkedHashSet<Word> result = new VocabularySetExpression(
                            expression,
                            (functionName, argument) -> loadSource(functionName, argument, vocabulary),
                            vocabulary).evaluate();
                    writeDeck(input, extractor, result);
                    break;
                } catch (IllegalArgumentException exception) {
                    System.out.println(exception.getMessage());
                }
            }
        } catch (InputClosedException ignored) {
            System.out.println("Interactive vocabulary builder cancelled.");
        }
    }

    private static void writeDeck(Scanner input, DictionaryExtractor extractor, Set<Word> result) {
        if (result.isEmpty()) {
            System.out.println("The resulting vocabulary set is empty; no deck was written.");
            return;
        }

        System.out.printf("%d words. Output file [%s]: ", result.size(), OUTPUT_FILENAME);
        String output = nextLine(input).trim();
        if (output.isEmpty()) {
            output = OUTPUT_FILENAME;
        }

        ExportOptions options = new ExportOptions(
                true,
                false,
                0,
                ChineseWordFinder.STRATEGY.ANSJ_SEGMENTATION,
                OutputFormat.ANKI,
                ChineseDeckStyler.HanziType.SIMP);
        DeckProducer deckProducer = new DeckProducer(extractor);
        FileUtils.writeToFile(deckProducer.produceDeckFromWords(result, options), output);
        System.out.printf("Wrote %d vocabulary cards to %s.%n", result.size(), output);
    }

    private static Set<Word> loadSource(
            String functionName, String argument, VocabularySetOperations vocabulary) {
        return switch (functionName.toLowerCase(Locale.ROOT)) {
            case "old_hsk" -> loadHskLevels(argument, vocabulary);
            case "open_cards" -> loadCards(argument, vocabulary);
            default -> throw new IllegalArgumentException("Unknown function '" + functionName + "'");
        };
    }

    private static Set<Word> loadHskLevels(String argument, VocabularySetOperations vocabulary) {
        if (argument.matches("1-[1-6]")) {
            return vocabulary.hskLevelsUpTo(Character.getNumericValue(argument.charAt(2)));
        }
        LinkedHashSet<Word> result = new LinkedHashSet<>();
        for (String level : argument.split(",")) {
            if (!level.matches("[1-6]")) {
                throw new IllegalArgumentException(
                        "old_hsk expects a level such as \"5\", a range such as \"1-5\", or levels such as \"1,3,5\"");
            }
            result = vocabulary.union(result, vocabulary.hskLevel(Integer.parseInt(level)));
        }
        return result;
    }

    private static Set<Word> loadCards(String filename, VocabularySetOperations vocabulary) {
        File file = new File(filename);
        if (file.isFile() && file.canRead()) {
            return vocabulary.fromFile(file);
        }
        throw new IllegalArgumentException("open_cards expects a readable file path");
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
