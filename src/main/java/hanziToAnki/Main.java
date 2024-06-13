package hanziToAnki;

import hanziToAnki.chinese.ChineseDictionaryExtractor;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) throws URISyntaxException, IOException {
        if (args.length == 0 || args[0].equals("-h") || args[0].equals("-help") || args[0].equals("--help")) {
            ArgParser.printUsage();
            return;
        }

        // later, user specifies "fr" language and we use factories to get right implementations
        DictionaryExtractor extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();

        DeckProducer deckProducer = new DeckProducer(extractor);

        var parsedArgs = ArgParser.parseArgs(args);




        if(!parsedArgs.options().pipes()) {
            for (String fileName : parsedArgs.fileNames()) {
                var outputLines = deckProducer.produceDeck(fileName, parsedArgs.options());
                FileUtils.writeToFile(outputLines, parsedArgs.outputFileName());
            }
        } else {
            List<String> lines = new ArrayList<>();
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            String s;
            while ((s = stdin.readLine()) != null) {
                lines.add(s);
            }
            List<String> outputLines = deckProducer.produceDeck(lines, parsedArgs.options());

            outputLines.forEach(System.out::println);
        }
    }
}
