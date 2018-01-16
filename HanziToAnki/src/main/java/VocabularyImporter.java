package HanziToAnki;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
public class VocabularyImporter{
  private static final String VOCAB_LOCATION="resources/vocab_lists/HSK";
  public static Set<Word> getAccumulativeHSKVocabulary(int HSKLevel){
    boolean[] levelsIncluded=new boolean[6];
    for(int i=0;i<HSKLevel;i++){
      levelsIncluded[i]=true;
    }
    return getHSKVocabulary(levelsIncluded);
  }
  public static Set<Word> getHSKVocabulary(boolean[] HSKLevelsIncluded){
    Set<Word> vocabSet = new HashSet();
    for(int i=0;i<6;i++){
      if(HSKLevelsIncluded[i]){
        vocabSet.addAll(getHSKVocabularyOneLevel(i+1));
      }
    }
    return vocabSet;
  }

  private static Set<Word> getHSKVocabularyOneLevel(int level){
     return getWordsFromNewlineSeparatedFile(VOCAB_LOCATION+level);
  }

  public static Set<Word> getWordsFromStringList(List<String> lines){
    Set<Word> vocabSet = new HashSet<Word>();
    for(String line:lines){
      Word wordToAdd=Extract.getWordFromChinese(line);
      if(wordToAdd!=null){
        vocabSet.add(wordToAdd);
      }
    }
    return vocabSet;
  }

  public static Set<Word> getWordsFromNewlineSeparatedFile(String filename){
    return getWordsFromStringList(FileUtils.fileToStringArray(filename));
  }
}
