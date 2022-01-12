package hanziToAnki;

import dictionary.VocabularyImporter;
import dictionary.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static hanziToAnki.OutputFormat.*;

public class DeckProducer {

    public static List<String> produceDeck(String filename, ExportOptions exportOptions, String outputFileName) {
        return produceDeck(FileUtils.fileToStringArray(filename), exportOptions, outputFileName);
    }

    public static List<String> produceDeck(List<String> lines, ExportOptions exportOptions, String outputFileName) {
        var words = generateWords(lines, exportOptions);

        if (words.isEmpty() && !lines.isEmpty()) {
            System.out.println("Please provide UTF-8 encoded files - other encodings (e.g. GBK, Big5 not currently supported");
            return new ArrayList<>();
        }

        var wordsToExclude = VocabularyImporter.getAccumulativeHSKVocabulary(exportOptions.hskLevelToExclude());
        words.removeAll(wordsToExclude);

        if (exportOptions.outputFormat() == ANKI) {
            return DeckFactory.generateDeck(words).getLines();
        }

        // We may support Memrise, Pleco, etc. at a later date
        System.out.println("Unrecognised output format");
        return new ArrayList<>();
    }

    private static Set<Word> generateWords(List<String> lines, ExportOptions options) {
        if (options.useWordList()) {
            return VocabularyImporter.getWordsFromStringList(lines);
        }

        if (options.useAllWords()) {
            return AnkiDeckProducer.getAnkiOutputForOneTwoThreeCharWords(lines);
        }

        return AnkiDeckProducer.getAnkiOutputFromSingleChars(lines);
    }
}
