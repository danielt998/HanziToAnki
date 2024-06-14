package hanziToAnki;

import static hanziToAnki.OutputFormat.*;

import hanziToAnki.chinese.ChineseGrader;
import hanziToAnki.chinese.ChineseWordFinder;
import hanziToAnki.deckStyler.DeckStylerFactory;
import hanziToAnki.deckStyler.PaperDeckStyler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


public class DeckProducer {

    private final DictionaryExtractor extractor;

    public DeckProducer(DictionaryExtractor extractor) {
        this.extractor = extractor;
    }

    public String producePaperDeck(List<String> lines, ExportOptions exportOptions) {
        var words = generateWords(lines, exportOptions);
        excludeWords(words, exportOptions);

        PaperDeckStyler styler = DeckStylerFactory.getPaperDeckStyler(words);
        styler.generateImageFile(words, exportOptions);

        return "test.jpg";
    }

    public List<String> producePlainTextDeck(String filename, ExportOptions exportOptions) {
        return producePlainTextDeck(FileUtils.fileToStringArray(filename), exportOptions);
    }

    public void excludeWords(Set<Word> words, ExportOptions exportOptions) {
        Grader grader = new ChineseGrader(extractor);
        var wordsToExclude = grader.getAccumulativeVocabulary(exportOptions.hskLevelToExclude());
        words.removeAll(wordsToExclude);
    }

    public List<String> producePlainTextDeck(List<String> lines, ExportOptions exportOptions) {
        var words = generateWords(lines, exportOptions);

        if (words.isEmpty() && !lines.isEmpty()) {
            return new ArrayList<>();
        }

        excludeWords(words, exportOptions);

        if (exportOptions.outputFormat() == ANKI) {
            var deckStyler = DeckStylerFactory.getDeckStyler(words);
            return deckStyler.style(words);
        }

        // We may support Memrise, Pleco, etc. at a later date
        System.out.println("Unrecognised output format");
        return new ArrayList<>();
    }

    private Set<Word> generateWords(List<String> lines, ExportOptions options) {
        if (options.useWordList()) { // todo think of more meaningful, easy-to-understand options for our users
            return lines.stream()
                    .map(s -> extractor.getWord(s))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
        }
        // TODO use our other options for grade-filtering (HSK level for Chinese)

        ChineseWordFinder wordFinder = new ChineseWordFinder(extractor);
        if (options.useAllWords()) {
            return wordFinder.findWords(options.strategy(), lines);
        }

        Set<Word> words = wordFinder.findMonograms(lines);
        if (words.isEmpty() && !lines.isEmpty()) {
            System.out.println("Please provide UTF-8 encoded files -"
                    + " other encodings (e.g. GBK, Big5 not currently supported");
        }
        return words;
    }
}
