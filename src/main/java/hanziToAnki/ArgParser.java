package hanziToAnki;

import java.util.ArrayList;
import java.util.List;

public class ArgParser {

    public record ParsedArgs(ExportOptions options, List<String>fileNames, String outputFileName) {}

    public static ParsedArgs parseArgs(String[] args) {
        List<String> fileNames = new ArrayList<>();
        String outputFileName = "output.csv"; //this or stdout?
        OutputFormat outputFormat = OutputFormat.ANKI;
        boolean useWordList = false;
        boolean allWords = true;
        int hskLevelToExtract = 0;

        for (int argNo = 0; argNo < args.length; argNo++)
            switch (args[argNo]) {
                case "-w", "--word-list" -> {
                    useWordList = true;
                    allWords = false;
                }
                case "-s", "--single-characters" -> {
                    allWords = false;
                    useWordList = false;
                }
                case "-hsk" -> hskLevelToExtract = Integer.parseInt(args[++argNo]);
                case "-o" -> outputFileName = args[++argNo];
                case "-f", "--format" -> {
                    String format = args[++argNo].toLowerCase();
                    outputFormat = switch (format) {
                        case "pleco" -> OutputFormat.PLECO;
                        case "memrise" -> OutputFormat.MEMRISE;
                        default -> OutputFormat.ANKI;
                    };
                }
                default -> fileNames.add(args[argNo]); // no flag specified - so this is the output file
            }

        var options = new ExportOptions(useWordList, allWords, hskLevelToExtract, outputFormat);
        return new ParsedArgs(options, fileNames, outputFileName);
    }
}
