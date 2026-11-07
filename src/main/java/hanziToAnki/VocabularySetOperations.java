package hanziToAnki;

import hanziToAnki.chinese.ChineseGrader;
import java.io.File;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads vocabulary sources and applies set operations while retaining the input order.
 */
public class VocabularySetOperations {
    private final DictionaryExtractor extractor;
    private final ChineseGrader grader;

    public VocabularySetOperations(DictionaryExtractor extractor) {
        this.extractor = extractor;
        this.grader = new ChineseGrader(extractor);
    }

    public LinkedHashSet<Word> fromFile(File file) {
        return fromLines(FileUtils.fileToStringArray(file));
    }

    public LinkedHashSet<Word> fromLines(List<String> lines) {
        LinkedHashSet<Word> words = new LinkedHashSet<>();
        for (String line : lines) {
            String entry = firstColumn(line);
            extractor.getWord(entry).ifPresent(words::add);
        }
        return words;
    }

    public LinkedHashSet<Word> hskLevel(int level) {
        return new LinkedHashSet<>(grader.getVocabulary(level));
    }

    public LinkedHashSet<Word> hskLevelsUpTo(int level) {
        return new LinkedHashSet<>(grader.getAccumulativeVocabulary(level));
    }

    public LinkedHashSet<Word> union(Set<Word> left, Set<Word> right) {
        LinkedHashSet<Word> result = new LinkedHashSet<>(left);
        result.addAll(right);
        return result;
    }

    public LinkedHashSet<Word> subtract(Set<Word> left, Set<Word> right) {
        LinkedHashSet<Word> result = new LinkedHashSet<>(left);
        result.removeAll(right);
        return result;
    }

    public LinkedHashSet<Word> intersect(Set<Word> left, Set<Word> right) {
        LinkedHashSet<Word> result = new LinkedHashSet<>(left);
        result.retainAll(right);
        return result;
    }

    private String firstColumn(String line) {
        String entry = line.startsWith("\uFEFF") ? line.substring(1) : line;
        int tabIndex = entry.indexOf('\t');
        return (tabIndex >= 0 ? entry.substring(0, tabIndex) : entry).trim();
    }
}
