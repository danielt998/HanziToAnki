package hanziToAnki;

import java.util.List;
import java.util.Set;

public interface Grader {

    Set<Word> noGrading(List<String> lines);

    Set<Word> getAccumulativeVocabulary(int level);
}