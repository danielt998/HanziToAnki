package hanziToAnki;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;

public class ArgParser {

    public static void printUsage() {
        System.out.println("Usage: java hanziToAnki.Main [OPTIONS] filename");
        System.out.println("options:");
        System.out.println(
                "\t-w --word-list:\tRead from an input file containing a list of words, separated"
                        + " by line breaks. Without this flag, individual characters are extracted.");
        System.out.println("\t-s --single-characters:\tdictHandler.Extract only single characters from the file.");
        System.out.println("\t-hsk <hsk level> Remove any words in any HSK levels up to and including"
                + " the given one.");
        System.out.println("\t-o <output filename> Override the default output file name");
        System.out.println("\t-f --format <output format> Override the default output file name\n"
                + "\t\tChoices are: " + Stream.of(OutputFormat.values())
                .map(OutputFormat::name)
                .collect(Collectors.joining(", ")));
    }

    public static ParsedArgs parseArgs(String[] args) {
        List<String> fileNames = new ArrayList<>();
        fileNames.add(args[args.length - 1]);
        String outputFileName = FilenameUtils.removeExtension(fileNames.get(0)) + ".tsv";
        OutputFormat outputFormat = OutputFormat.ANKI;
        boolean useWordList = false;
        boolean allWords = true;
        int hskLevelToExtract = 0;

        for (int argNo = 0; argNo < args.length - 1; argNo++) {
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
        }

        var options = new ExportOptions(useWordList, allWords, hskLevelToExtract, outputFormat);
        return new ParsedArgs(options, fileNames, outputFileName);
    }

    public record ParsedArgs(ExportOptions options, List<String> fileNames, String outputFileName) {
    }
}
