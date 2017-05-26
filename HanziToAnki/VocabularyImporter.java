import java.util.Set;
import java.util.HashSet;
public class VocabularyImporter{
  public static Set<Word> getHSKVocabulary(boolean[] HSKLevelsIncluded){
    Set<Word> vocabSet = new HashSet();
    for(int i=0;i<6;i++){
      vocabSet.addAll(getHSKVocabularyOneLevel(i+1));
    }
    return vocabSet;
  }

  private static Set<Word> getHSKVocabularyOneLevel(int level){
     return getWordsFromNewlineSeparatedFile("resources/vocablist/HSK"+level);
  }

  public static Set<Word> getWordsFromNewlineSeparatedFile(String filename){
    Set<Word> vocabSet = new HashSet<Word>();
    for(String line:FileUtils.fileToStringArray(filename)){
      Word wordToAdd=Extract.getWordFromChinese(line);
      if(wordToAdd!=null){
        vocabSet.add(wordToAdd);
      }
    }
    return vocabSet; 
  }
}
