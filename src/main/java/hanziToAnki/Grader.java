package hanziToAnki;

import java.util.List;
import java.util.Set;

public interface Grader {

    Set<Word> getAccumulativeVocabulary(int level);
}