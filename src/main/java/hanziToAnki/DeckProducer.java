package hanziToAnki;

import hanziToAnki.chinese.ChineseAnkiExporter;
import hanziToAnki.chinese.ChineseGrader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static hanziToAnki.OutputFormat.ANKI;

public class DeckProducer {

    public List<String> produceDeck(String filename, ExportOptions exportOptions, String outputFileName) {
        return produceDeck(FileUtils.fileToStringArray(filename), exportOptions, outputFileName);
    }

    public List<String> produceDeck(List<String> lines, ExportOptions exportOptions, String outputFileName) {
        var words = generateWords(lines, exportOptions);

        if (words.isEmpty() && !lines.isEmpty()) {
            System.out.println("Please provide UTF-8 encoded files - other encodings (e.g. GBK, Big5 not currently supported");
            return new ArrayList<>();
        }

        Grader grader = new ChineseGrader(null);
        var wordsToExclude = grader.getAccumulativeVocabulary(exportOptions.hskLevelToExclude());
        words.removeAll(wordsToExclude);

        if (exportOptions.outputFormat() == ANKI) {
            var deck = DeckFactory.getDeck(words);
            return deck.generate(words);
        }

        // We may support Memrise, Pleco, etc. at a later date
        System.out.println("Unrecognised output format");
        return new ArrayList<>();
    }

    private Set<Word> generateWords(List<String> lines, ExportOptions options) {
        ChineseAnkiExporter exporter = new ChineseAnkiExporter(null); // TODO pass in a dictionary extractor

        if (options.useWordList()) {
            Grader grader = new ChineseGrader(null); // TODO pass in a dictionary extractor
            return grader.noGrading(lines);
        }

        if (options.useAllWords()) {
            return exporter.getAnkiOutputForOneTwoThreeCharWords(lines);
        }

        return exporter.getAnkiOutputFromSingleChars(lines);
    }
}
