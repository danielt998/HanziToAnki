package hanziToAnki.french;

import hanziToAnki.Grader;
import hanziToAnki.Word;

import java.util.List;
import java.util.Set;

public class FrenchGrader implements Grader {
    @Override
    public Set<Word> noGrading(List<String> lines) {
        return null;
    }

    @Override
    public Set<Word> getAccumulativeVocabulary(int level) {
        return null;
    }
}