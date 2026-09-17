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
            System.out.println("Sources: a vocabulary/Anki TSV file path, hsk1 through hsk6, or hsk1-5 for cumulative HSK.");
            while (true) {
                System.out.print("Set expression: ");
                String expression = nextLine(input).trim();
                try {
                    LinkedHashSet<Word> result = new VocabularySetExpression(
                            expression,
                            source -> loadSource(source, vocabulary),
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

    private static Set<Word> loadSource(String source, VocabularySetOperations vocabulary) {
        String normalisedSource = source.toLowerCase(Locale.ROOT);
        if (normalisedSource.matches("hsk[1-6]")) {
            return vocabulary.hskLevel(Character.getNumericValue(normalisedSource.charAt(3)));
        }
        if (normalisedSource.matches("hsk1-[1-6]")) {
            return vocabulary.hskLevelsUpTo(Character.getNumericValue(normalisedSource.charAt(5)));
        }

        File file = new File(source);
        if (file.isFile() && file.canRead()) {
            return vocabulary.fromFile(file);
        }
        throw new IllegalArgumentException("Source must be a readable file path, hsk1 through hsk6, or hsk1-5");
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
