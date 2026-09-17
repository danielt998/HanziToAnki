package hanziToAnki;

import hanziToAnki.chinese.ChineseDictionaryExtractor;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        if (args.length == 1 && (args[0].equals("-i") || args[0].equals("--interactive"))) {
            InteractiveVocabularyPrompt.run();
            return;
        }

        if (args.length == 0 || args[0].equals("-h") || args[0].equals("-help") || args[0].equals("--help")) {
            ArgParser.printUsage();
            return;
        }

        DictionaryExtractor extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();

        DeckProducer deckProducer = new DeckProducer(extractor);

        ArgParser.ParsedArgs parsedArgs = ArgParser.parseArgs(args);
        for (String fileName : parsedArgs.fileNames()) {
            if (parsedArgs.options().outputFormat() == OutputFormat.PDF_FLASHCARDS) {
                List<String> inputLines = FileUtils.fileToStringArray(fileName);
                byte[] pdfContent = deckProducer.producePdfFlashcards(inputLines, parsedArgs.options(), parsedArgs.cardStyle(), parsedArgs.useToneColors());
                Files.write(Paths.get(parsedArgs.outputFileName()), pdfContent);
                logger.info("Generated PDF flashcards: {}", parsedArgs.outputFileName());
            } else {
                List<String> outputLines = deckProducer.produceDeck(fileName, parsedArgs.options());
                FileUtils.writeToFile(outputLines, parsedArgs.outputFileName());
            }
        }
    }
}
