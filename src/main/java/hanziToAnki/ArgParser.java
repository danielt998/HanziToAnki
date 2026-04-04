package hanziToAnki;


import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import hanziToAnki.chinese.ChineseDeckStyler;
import hanziToAnki.chinese.ChineseWordFinder;
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
        System.out.println("\t-t --strategy <strategy>\tSpecify the word finding strategy. See" +
                " ChineseWordFinder.Strategy enum for details.");
        System.out.println("\t-o <output filename> Override the default output file name");
        System.out.println("\t-f --format <output format> Override the default output file name\n"
                + "\t\tChoices are: " + Stream.of(OutputFormat.values())
                .map(OutputFormat::name)
                .collect(Collectors.joining(", ")));
        System.out.println("\t-c --char-type <char type> Specify the type of character\n"
        + "\t\tChoices are: " + Stream.of(ChineseDeckStyler.HanziType.values())
                .map(ChineseDeckStyler.HanziType::name)
                .collect(Collectors.joining(", ")));
    }

    public static ParsedArgs parseArgs(String[] args) {
        List<String> fileNames = new ArrayList<>();
        fileNames.add(args[args.length - 1]);
        String outputFileName = FilenameUtils.removeExtension(fileNames.get(0)) + ".tsv";
        OutputFormat outputFormat = OutputFormat.ANKI;
        ChineseDeckStyler.HanziType charType = ChineseDeckStyler.HanziType.SIMP;
        boolean useWordList = false;
        boolean allWords = true;
        int hskLevelToExclude = 0;
        ChineseWordFinder.STRATEGY strategy = ChineseWordFinder.STRATEGY.ANSJ_SEGMENTATION;

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
                case "-hsk" -> {
                    try {
                        hskLevelToExclude = Integer.parseInt(args[++argNo]);
                        if (hskLevelToExclude < 0 || hskLevelToExclude > 6) {
                            System.out.println("Invalid HSK level: " + hskLevelToExclude + ". Must be 0-6.");
                            hskLevelToExclude = 0;
                        }
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println("Error: -hsk requires a level argument");
                    } catch (NumberFormatException e) {
                        System.out.println("Error: HSK level must be a number");
                    }
                }
                case "-o" -> outputFileName = args[++argNo];
                case "-t", "--strategy" -> {
                    try {
                        strategy = ChineseWordFinder.STRATEGY.getStrategy(Integer.parseInt(args[++argNo]));
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println("Error: -t/--strategy requires a strategy number argument");
                    } catch (NumberFormatException e) {
                        System.out.println("Error: Strategy must be a number");
                    }
                }
                case "-f", "--format" -> {
                    try {
                        String format = args[++argNo].toLowerCase();
                        outputFormat = switch (format) {
                            case "pleco" -> OutputFormat.PLECO;
                            case "memrise" -> OutputFormat.MEMRISE;
                            default -> OutputFormat.ANKI;
                        };
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println("Error: -f/--format requires a format argument");
                    }
                }
                case "-c", "--char-type" -> {
                    try {
                        String type = args[++argNo].toLowerCase();
                        charType = switch (type) {
                            case "simp", "simplified" -> ChineseDeckStyler.HanziType.SIMP;
                            case "trad", "traditional" -> ChineseDeckStyler.HanziType.TRAD;
                            case "both" -> ChineseDeckStyler.HanziType.SIMP_AND_TRAD;
                            default -> ChineseDeckStyler.HanziType.SIMP;
                        };
                    } catch (IndexOutOfBoundsException e) {
                        System.out.println("Error: -c/--char-type requires a type argument");
                    }
                }
                default -> fileNames.add(args[argNo]);
            }
        }

        var options = new ExportOptions(useWordList, allWords, hskLevelToExclude, strategy, outputFormat, charType);
        return new ParsedArgs(options, fileNames, outputFileName);
    }

    public record ParsedArgs(ExportOptions options, List<String> fileNames, String outputFileName) {
    }
}
