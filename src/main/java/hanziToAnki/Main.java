package hanziToAnki;

import hanziToAnki.chinese.ChineseDictionaryExtractor;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws URISyntaxException, IOException {
        if (args.length == 0 || args[0].equals("-h") || args[0].equals("-help") || args[0].equals("--help")) {
            ArgParser.printUsage();
            return;
        }

        DictionaryExtractor extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();

        DeckProducer deckProducer = new DeckProducer(extractor);

        var parsedArgs = ArgParser.parseArgs(args);
        for (String fileName : parsedArgs.fileNames()) {
            if (parsedArgs.options().outputFormat() == OutputFormat.PDF_FLASHCARDS) {
                var inputLines = FileUtils.fileToStringArray(fileName);
                byte[] pdfContent = deckProducer.producePdfFlashcards(inputLines, parsedArgs.options(), parsedArgs.cardStyle(), parsedArgs.useToneColors());
                Files.write(Paths.get(parsedArgs.outputFileName()), pdfContent);
                logger.info("Generated PDF flashcards: {}", parsedArgs.outputFileName());
            } else {
                var outputLines = deckProducer.produceDeck(fileName, parsedArgs.options());
                FileUtils.writeToFile(outputLines, parsedArgs.outputFileName());
            }
        }
    }
}
