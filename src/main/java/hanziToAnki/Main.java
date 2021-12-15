package hanziToAnki;

import dictionary.Extract;
import dictionary.VocabularyImporter;
import dictionary.ChineseWord;
import hanziToAnki.decks.Deck;
import hanziToAnki.decks.DeckFactory;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/*TODO:
 *  move more of the code to use HashSets, some of it still allows duplicates
 *  move stuff out of the main method and find some  way to make it a little more generic
 *  in getCharsFromFile, 1) rename "acc" (accumulator), try StringBuilder chineseCharsOnly (instead of String)...
 *  ...See if StringBuilder is much faster than String for the above
 */
public class Main {
    private static char[] getCharsFromList(List<String> lines) {
        StringBuilder fullString = new StringBuilder();
        for (String line : lines) {
            fullString.append(line);
        }
        char[] allChars = fullString.toString().toCharArray();
        StringBuilder chineseCharsOnly = new StringBuilder();
        for (char c : allChars) {
            if (Character.UnicodeScript.of(c) == Character.UnicodeScript.HAN) {
                chineseCharsOnly.append(c);
            }
        }
        return chineseCharsOnly.toString().toCharArray();
    }

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
        System.out.println("\t-f --format <output format> Override the default output file name\n" +
                "\t\tChoices are: " + Stream.of(OutputFormat.values())
                .map(OutputFormat::name)
                .collect(Collectors.joining(", ")));
    }

    public static void produceDeck(List<String> lines, ExportOptions exportOptions, String outputFileName) {
        Set<ChineseWord> words = new HashSet<ChineseWord>();
        if (exportOptions.useWordList()) {
            words.addAll(VocabularyImporter.getWordsFromStringList(lines));
        } else if (exportOptions.useAllWords()) {
            words.addAll(getAnkiOutputForOneTwoThreeCharWords(lines));
        } else {
            words.addAll(getAnkiOutputFromSingleChars(lines));
        }

        if (words.isEmpty() && !lines.isEmpty()) {
            System.out.println("Please provide UTF-8 encoded files - other encodings (e.g. GBK, Big5 not currently supported");
            return;
        }

        words.removeAll(VocabularyImporter.getAccumulativeHSKVocabulary(exportOptions.hskLevelToExclude()));

        List<String> outputLines;
        Deck deck = DeckFactory.getDeck(words);
        deck.generate(words);

        switch (exportOptions.outputFormat()) {
            case ANKI -> outputLines = deck.getLines();
//            case PLECO -> outputLines = PlecoDeckFactory.generateDeck(words).getLines(); // TODO find where PlecoDeckFactory is
//            case MEMRISE -> outputLines = MemriseDeckFactory...
            default -> {
                System.out.println("unrecognised output format");
                outputLines = new ArrayList<>();
            }
        }

        FileUtils.writeToFile(outputLines, outputFileName);
    }

    public static void produceDeck(String filename, ExportOptions exportOptions, String outputFileName) {
        produceDeck(FileUtils.fileToStringArray(filename), exportOptions, outputFileName);
    }

    public static void main(String[] args) throws URISyntaxException {
        if (args.length == 0 || args[0].equals("-h") || args[0].equals("-help") || args[0].equals("--help")) {
            printUsage();
            return;
        }

        Extract.readInDictionary();

        var parsedArgs = ArgParser.parseArgs(args);
        for (String fileName : parsedArgs.fileNames()) {
            produceDeck(fileName, parsedArgs.options(), parsedArgs.outputFileName());
        }
    }

    private static Set<ChineseWord> getAnkiOutputForOneTwoThreeCharWords(List<String> list) {
        char[] charArray = getCharsFromList(list);
        return getAnkiOutputForOneTwoThreeCharWordsWithCharArr(charArray);
    }

    private static Set<ChineseWord> getAnkiOutputForOneTwoThreeCharWordsWithCharArr(char[] charArray) {
        Set<ChineseWord> words = new HashSet<ChineseWord>();
        for (int i = 0; i < charArray.length; i++) {
            String word = "" + charArray[i];
            boolean wordUsed = false;
            if (i + 1 < charArray.length) {
                ChineseWord wordTwoChars = Extract.getWordFromChinese(word + charArray[i + 1]);
                if (wordTwoChars != null) {
                    words.add(wordTwoChars);
                    wordUsed = true;
                    i++;
                }
            }
            if (i + 2 - 1 < charArray.length) {//TODO:fix this
                ChineseWord wordThreeChars = Extract.getWordFromChinese(word + charArray[i + 1 - 1] + charArray[i + 2 - 1]);
                if (wordThreeChars != null) {
                    words.add(wordThreeChars);
                    wordUsed = true;
                    i++;
                }
            }
            if (!wordUsed) {//iff character is not used as part of any other word, we print it
                //TODO:consider whether this should be the behaviour and how arguments might be restructured
                ChineseWord wordSingleChar = Extract.getWordFromChinese(word);
                if (wordSingleChar != null) {
                    words.add(wordSingleChar);
                }
            }
        }
        return words;
    }

    private static Set<ChineseWord> getAnkiOutputFromSingleCharsWithCharArr(char[] charArray) {
        Set<ChineseWord> words = new HashSet<ChineseWord>();
        for (char c : charArray) {
            ChineseWord word = Extract.getWordFromChinese(c);
            words.add(word);
        }
        return words;
    }

    private static Set<ChineseWord> getAnkiOutputFromSingleChars(List<String> lines) {
        char[] charArray = getCharsFromList(lines);
        return getAnkiOutputFromSingleCharsWithCharArr(charArray);
    }
}
