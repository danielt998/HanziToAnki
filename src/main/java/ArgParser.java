import java.util.Arrays;
import java.util.List;

public class ArgParser {

    public record ParsedArgs(ExportOptions options, List<String>fileNames, String outputFileName) {}

    public static ParsedArgs parseArgs(String[] args) {
        List<String> fileNames = Arrays.asList(args[args.length - 1]);
        String outputFileName = FileUtils.removeExtensionFromFileName(fileNames.get(0)) + ".csv";
        OutputFormat outputFormat = OutputFormat.ANKI;
        boolean useWordList = false;
        boolean allWords = true;
        int hskLevelToExtract = 0;

        for (int argno = 0; argno < args.length - 1; argno++)
            switch (args[argno]) {
                case "-w", "--word-list" -> {
                    useWordList = true;
                    allWords = false;
                }
                case "-s", "--single-characters" -> {
                    allWords = false;
                    useWordList = false;
                }
                case "-hsk" -> hskLevelToExtract = Integer.parseInt(args[++argno]);
                case "-o" -> outputFileName = args[++argno];
                case "-f", "--format" -> {
                    String format = args[++argno].toLowerCase();
                    outputFormat = switch (format) {
                        case "pleco" -> OutputFormat.PLECO;
                        case "memrise" -> OutputFormat.MEMRISE;
                        default -> OutputFormat.ANKI;
                    };
                }
                default -> fileNames.add(args[argno]); // no flag specified - so this is the output file
            }

        var options = new ExportOptions(useWordList, allWords, hskLevelToExtract, outputFormat);
        return new ParsedArgs(options, fileNames, outputFileName);
    }
}
