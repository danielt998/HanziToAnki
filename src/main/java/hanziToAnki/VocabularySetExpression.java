package hanziToAnki;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

public final class VocabularySetExpression {
    private final String expression;
    private final Function<String, Set<Word>> sourceLoader;
    private final VocabularySetOperations operations;
    private int position;

    public VocabularySetExpression(
            String expression,
            Function<String, Set<Word>> sourceLoader,
            VocabularySetOperations operations) {
        this.expression = expression;
        this.sourceLoader = sourceLoader;
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

        String source = parseSource();
        try {
            return new LinkedHashSet<>(sourceLoader.apply(source));
        } catch (IllegalArgumentException exception) {
            throw error(exception.getMessage());
        }
    }

    private String parseSource() {
        skipWhitespace();
        if (position >= expression.length()) {
            throw error("Expected a source");
        }
        char firstCharacter = expression.charAt(position);
        if (firstCharacter == '"' || firstCharacter == '\'') {
            int start = ++position;
            while (position < expression.length() && expression.charAt(position) != firstCharacter) {
                position++;
            }
            if (position == expression.length()) {
                throw error("Unclosed quoted file path");
            }
            String source = expression.substring(start, position);
            position++;
            return source;
        }

        String hskSource = parseHskSource();
        if (hskSource != null) {
            return hskSource;
        }

        throw error("File paths must be enclosed in single or double quotes");
    }

    private String parseHskSource() {
        int start = position;
        if (position + 6 <= expression.length()
                && expression.regionMatches(true, position, "hsk1-", 0, 5)
                && expression.charAt(position + 5) >= '1'
                && expression.charAt(position + 5) <= '6'
                && isSourceBoundary(position + 6)) {
            position += 6;
            return expression.substring(start, position);
        }
        if (position + 4 <= expression.length()
                && expression.regionMatches(true, position, "hsk", 0, 3)
                && expression.charAt(position + 3) >= '1'
                && expression.charAt(position + 3) <= '6'
                && isSourceBoundary(position + 4)) {
            position += 4;
            return expression.substring(start, position);
        }
        return null;
    }

    private boolean isSourceBoundary(int sourceEnd) {
        return sourceEnd == expression.length()
                || Character.isWhitespace(expression.charAt(sourceEnd))
                || "+-&()".indexOf(expression.charAt(sourceEnd)) >= 0;
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
