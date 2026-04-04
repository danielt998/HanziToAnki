package hanziToAnki;

import static hanziToAnki.OutputFormat.ANKI;

import hanziToAnki.chinese.ChineseGrader;
import hanziToAnki.chinese.ChineseWordFinder;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DeckProducer {
    private static final Logger logger = LoggerFactory.getLogger(DeckProducer.class);
    private final DictionaryExtractor extractor;

    public DeckProducer(DictionaryExtractor extractor) {
        this.extractor = extractor;
    }

    public List<String> produceDeck(String filename, ExportOptions exportOptions) {
        return produceDeck(FileUtils.fileToStringArray(filename), exportOptions);
    }

    public List<String> produceDeck(List<String> lines, ExportOptions exportOptions) {
        var words = generateWords(lines, exportOptions);

        if (words.isEmpty() && !lines.isEmpty()) {
            logger.warn("No words extracted. Please provide UTF-8 encoded files - "
                    + "other encodings (e.g. GBK, Big5) are not currently supported");
            return new ArrayList<>();
        }

        Grader grader = new ChineseGrader(extractor);
        var wordsToExclude = grader.getAccumulativeVocabulary(exportOptions.hskLevelToExclude());
        words.removeAll(wordsToExclude);

        if (exportOptions.outputFormat() == ANKI) {
            var deckStyler = DeckStylerFactory.getDeckStyler(words, exportOptions.hanziType());
            return deckStyler.style(words);
        }

        logger.warn("Unrecognised output format: {}", exportOptions.outputFormat());
        return new ArrayList<>();
    }

    private Set<Word> generateWords(List<String> lines, ExportOptions options) {
        if (options.useWordList()) {
            return lines.stream()
                    .map(s -> extractor.getWord(s))
                    .flatMap(java.util.Optional::stream)
                    .collect(Collectors.toSet());
        }

        ChineseWordFinder wordFinder = new ChineseWordFinder(extractor);
        if (options.useAllWords()) {
            return wordFinder.findWords(options.strategy(), lines);
        }

        return wordFinder.findMonograms(lines);
    }
}
