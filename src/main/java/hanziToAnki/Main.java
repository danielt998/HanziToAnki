package hanziToAnki;

import hanziToAnki.chinese.ChineseDictionaryExtractor;
import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        if (args.length == 0 || args[0].equals("-h") || args[0].equals("-help") || args[0].equals("--help")) {
            ArgParser.printUsage();
            return;
        }

        // later, user specifies "fr" language and we use factories to get right implementations
        DictionaryExtractor extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();

        DeckProducer deckProducer = new DeckProducer(extractor);

        var parsedArgs = ArgParser.parseArgs(args);
        for (String fileName : parsedArgs.fileNames()) {
            var outputLines = deckProducer.produceDeck(fileName, parsedArgs.options());
            FileUtils.writeToFile(outputLines, parsedArgs.outputFileName());
        }
    }
}
