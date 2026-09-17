package hanziToAnki;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class VocabularySetExpression {
    private final String expression;
    private final BiFunction<String, String, Set<Word>> sourceLoader;
    private final Function<String, Set<Word>> variableLoader;
    private final VocabularySetOperations operations;
    private int position;

    public VocabularySetExpression(
            String expression,
            BiFunction<String, String, Set<Word>> sourceLoader,
            Function<String, Set<Word>> variableLoader,
            VocabularySetOperations operations) {
        this.expression = expression;
        this.sourceLoader = sourceLoader;
        this.variableLoader = variableLoader;
        this.operations = operations;
    }

    public LinkedHashSet<Word> evaluate() {
        LinkedHashSet<Word> result = parseUnionAndDifference();
        skipWhitespace();
        if (position != expression.length()) {
            throw error("Expected a set operator");
        }
        return result;
    }

    private LinkedHashSet<Word> parseUnionAndDifference() {
        LinkedHashSet<Word> result = parseIntersection();
        while (true) {
            skipWhitespace();
            if (consume('+')) {
                result = operations.union(result, parseIntersection());
            } else if (consume('-')) {
                result = operations.subtract(result, parseIntersection());
            } else {
                return result;
            }
        }
    }

    private LinkedHashSet<Word> parseIntersection() {
        LinkedHashSet<Word> result = parsePrimary();
        while (true) {
            skipWhitespace();
            if (!consume('&')) {
                return result;
            }
            result = operations.intersect(result, parsePrimary());
        }
    }

    private LinkedHashSet<Word> parsePrimary() {
        skipWhitespace();
        if (consume('(')) {
            LinkedHashSet<Word> result = parseUnionAndDifference();
            skipWhitespace();
            if (!consume(')')) {
                throw error("Expected ')'");
            }
            return result;
        }

        String name = parseName();
        skipWhitespace();
        if (!consume('(')) {
            try {
                return new LinkedHashSet<>(variableLoader.apply(name));
            } catch (IllegalArgumentException exception) {
                throw error(exception.getMessage());
            }
        }
        String argument = parseFunctionArgument();
        skipWhitespace();
        if (!consume(')')) {
            throw error("Expected ')' after function argument");
        }
        try {
            return new LinkedHashSet<>(sourceLoader.apply(name, argument));
        } catch (IllegalArgumentException exception) {
            throw error(exception.getMessage());
        }
    }

    private String parseName() {
        skipWhitespace();
        if (position >= expression.length()) {
            throw error("Expected a variable or function");
        }
        int start = position;
        while (position < expression.length()
                && (Character.isLetterOrDigit(expression.charAt(position)) || expression.charAt(position) == '_')) {
            position++;
        }
        if (start == position) {
            throw error("Expected a variable or function name");
        }
        return expression.substring(start, position);
    }

    private String parseFunctionArgument() {
        skipWhitespace();
        if (position >= expression.length()) {
            throw error("Expected a function argument");
        }
        char firstCharacter = expression.charAt(position);
        if (firstCharacter == '"' || firstCharacter == '\'') {
            int start = ++position;
            while (position < expression.length() && expression.charAt(position) != firstCharacter) {
                position++;
            }
            if (position == expression.length()) {
                throw error("Unclosed quoted function argument");
            }
            String argument = expression.substring(start, position);
            position++;
            return argument;
        }
        int start = position;
        while (position < expression.length() && expression.charAt(position) != ')') {
            position++;
        }
        String argument = expression.substring(start, position).trim();
        if (argument.isEmpty()) {
            throw error("Expected a function argument");
        }
        return argument;
    }

    private void skipWhitespace() {
        while (position < expression.length() && Character.isWhitespace(expression.charAt(position))) {
            position++;
        }
    }

    private boolean consume(char expected) {
        if (position < expression.length() && expression.charAt(position) == expected) {
            position++;
            return true;
        }
        return false;
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " at character " + (position + 1));
    }
}
