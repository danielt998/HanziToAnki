package hanziToAnki;

import dictionary.Extract;

import java.net.URISyntaxException;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        if (args.length == 0 || args[0].equals("-h") || args[0].equals("-help") || args[0].equals("--help")) {
            ArgParser.printUsage();
            return;
        }

        Extract.readInDictionary();

        var parsedArgs = ArgParser.parseArgs(args);
        for (String fileName : parsedArgs.fileNames()) {
            var outputLines = DeckProducer.produceDeck(fileName, parsedArgs.options(), parsedArgs.outputFileName());
            FileUtils.writeToFile(outputLines, parsedArgs.outputFileName());
        }
    }
}
