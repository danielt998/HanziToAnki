package hanziToAnki;

import hanziToAnki.chinese.ChineseWordFinder;
import hanziToAnki.chinese.ChineseGrader;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static hanziToAnki.OutputFormat.ANKI;

public class DeckProducer {

    private final DictionaryExtractor extractor;

    public DeckProducer(DictionaryExtractor extractor) {
        this.extractor = extractor;
    }

    public List<String> produceDeck(String filename, ExportOptions exportOptions, String outputFileName) {
        return produceDeck(FileUtils.fileToStringArray(filename), exportOptions, outputFileName);
    }

    public List<String> produceDeck(List<String> lines, ExportOptions exportOptions, String outputFileName) {
        var words = generateWords(lines, exportOptions);

        if (words.isEmpty() && !lines.isEmpty()) {
            System.out.println("Please provide UTF-8 encoded files - other encodings (e.g. GBK, Big5 not currently supported");
            return new ArrayList<>();
        }

        Grader grader = new ChineseGrader(extractor);
        var wordsToExclude = grader.getAccumulativeVocabulary(exportOptions.hskLevelToExclude());
        words.removeAll(wordsToExclude);

        if (exportOptions.outputFormat() == ANKI) {
            var deckStyler = DeckStylerFactory.getDeck(words);
            return deckStyler.style(words);
        }

        // We may support Memrise, Pleco, etc. at a later date
        System.out.println("Unrecognised output format");
        return new ArrayList<>();
    }

    private Set<Word> generateWords(List<String> lines, ExportOptions options) {
        if (options.useWordList()) { // todo think of more meaningful, easy-to-understand options for our users
            return lines.stream()
                    .map(s-> extractor.getWord(s))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        // TODO use our other options for grade-filtering (HSK level for Chinese)

        ChineseWordFinder wordFinder = new ChineseWordFinder(extractor);
        if (options.useAllWords()) {
            return wordFinder.findMonoBiTriGrams(lines);
        }

        return wordFinder.findMonograms(lines);
    }
}
