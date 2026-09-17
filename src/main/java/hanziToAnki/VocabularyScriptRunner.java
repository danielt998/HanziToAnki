package hanziToAnki;

import hanziToAnki.chinese.ChineseDeckStyler;
import hanziToAnki.chinese.ChineseWordFinder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class VocabularyScriptRunner {
    private static final Pattern ASSIGNMENT = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)\\s*=\\s*(.+)$");
    private final DictionaryExtractor extractor;
    private final VocabularySetOperations vocabulary;
    private final Map<String, LinkedHashSet<Word>> variables = new LinkedHashMap<>();

    public VocabularyScriptRunner(DictionaryExtractor extractor) {
        this.extractor = extractor;
        this.vocabulary = new VocabularySetOperations(extractor);
    }

    public String execute(String statement) {
        String command = removeComment(statement).trim();
        if (command.isEmpty()) {
            return "";
        }

        Matcher assignment = ASSIGNMENT.matcher(command);
        if (assignment.matches()) {
            String name = assignment.group(1);
            LinkedHashSet<Word> result = evaluate(assignment.group(2));
            variables.put(name, result);
            return name + " = " + result.size() + " words";
        }
        if (command.startsWith("write_cards")) {
            return writeCards(command);
        }
        return evaluate(command).size() + " words";
    }

    public void runScript(Path script) throws IOException {
        var lines = Files.readAllLines(script);
        for (int index = 0; index < lines.size(); index++) {
            try {
                String result = execute(lines.get(index));
                if (!result.isEmpty()) {
                    System.out.println(result);
                }
            } catch (IllegalArgumentException exception) {
                throw new IllegalArgumentException(script + ":" + (index + 1) + ": " + exception.getMessage(), exception);
            }
        }
    }

    public Set<String> variableNames() {
        return Set.copyOf(variables.keySet());
    }

    private LinkedHashSet<Word> evaluate(String expression) {
        return new VocabularySetExpression(
                expression,
                this::loadFunction,
                this::loadVariable,
                vocabulary).evaluate();
    }

    private Set<Word> loadFunction(String functionName, String argument) {
        return switch (functionName.toLowerCase(Locale.ROOT)) {
            case "old_hsk" -> loadHskLevels(argument);
            case "open_cards" -> loadCards(argument);
            default -> throw new IllegalArgumentException("Unknown function '" + functionName + "'");
        };
    }

    private Set<Word> loadVariable(String name) {
        Set<Word> variable = variables.get(name);
        if (variable == null) {
            throw new IllegalArgumentException("Unknown variable '" + name + "'");
        }
        return variable;
    }

    private Set<Word> loadHskLevels(String argument) {
        if (argument.matches("1-[1-6]")) {
            return vocabulary.hskLevelsUpTo(Character.getNumericValue(argument.charAt(2)));
        }
        LinkedHashSet<Word> result = new LinkedHashSet<>();
        for (String level : argument.split(",")) {
            if (!level.matches("[1-6]")) {
                throw new IllegalArgumentException(
                        "old_hsk expects a level such as \"5\", a range such as \"1-5\", or levels such as \"1,3,5\"");
            }
            result = vocabulary.union(result, vocabulary.hskLevel(Integer.parseInt(level)));
        }
        return result;
    }

    private Set<Word> loadCards(String filename) {
        File file = new File(filename);
        if (!file.isFile() || !file.canRead()) {
            throw new IllegalArgumentException("open_cards expects a readable file path");
        }
        return vocabulary.fromFile(file);
    }

    private String writeCards(String statement) {
        String arguments = functionArguments(statement, "write_cards");
        int separator = topLevelComma(arguments);
        if (separator < 0) {
            throw new IllegalArgumentException("write_cards expects a set expression and an output filename");
        }
        LinkedHashSet<Word> result = evaluate(arguments.substring(0, separator));
        if (result.isEmpty()) {
            return "The resulting vocabulary set is empty; no deck was written.";
        }

        String filename = quotedFilename(arguments.substring(separator + 1).trim());
        ExportOptions options = new ExportOptions(
                true, false, 0, ChineseWordFinder.STRATEGY.ANSJ_SEGMENTATION,
                OutputFormat.ANKI, ChineseDeckStyler.HanziType.SIMP);
        FileUtils.writeToFile(new DeckProducer(extractor).produceDeckFromWords(result, options), filename);
        return "Wrote " + result.size() + " vocabulary cards to " + filename + ".";
    }

    private String functionArguments(String statement, String functionName) {
        int openingParenthesis = statement.indexOf('(');
        if (openingParenthesis != functionName.length() || !statement.endsWith(")")) {
            throw new IllegalArgumentException(functionName + " expects parentheses");
        }
        return statement.substring(openingParenthesis + 1, statement.length() - 1);
    }

    private int topLevelComma(String value) {
        int parentheses = 0;
        char quote = 0;
        for (int index = 0; index < value.length(); index++) {
            char character = value.charAt(index);
            if (quote != 0) {
                if (character == quote) {
                    quote = 0;
                }
            } else if (character == '"' || character == '\'') {
                quote = character;
            } else if (character == '(') {
                parentheses++;
            } else if (character == ')') {
                parentheses--;
            } else if (character == ',' && parentheses == 0) {
                return index;
            }
        }
        return -1;
    }

    private String quotedFilename(String value) {
        if (value.length() < 2 || (value.charAt(0) != '"' && value.charAt(0) != '\'')
                || value.charAt(value.length() - 1) != value.charAt(0)) {
            throw new IllegalArgumentException("write_cards output filename must be quoted");
        }
        return value.substring(1, value.length() - 1);
    }

    private String removeComment(String line) {
        char quote = 0;
        for (int index = 0; index < line.length(); index++) {
            char character = line.charAt(index);
            if (quote != 0) {
                if (character == quote) {
                    quote = 0;
                }
            } else if (character == '"' || character == '\'') {
                quote = character;
            } else if (character == '#') {
                return line.substring(0, index);
            }
        }
        return line;
    }
}
