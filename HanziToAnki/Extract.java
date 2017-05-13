import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
/*Notes:
  This is a mess of a piece of code that I pulled from another of my projects, it needs sorting out
  Also, it has some useful methods that are of no use for this particular project, so it might be nice
  to create a separate linked GitHub project or something for it later
  Another note:for now, it will only handle unique characters, not words composed of chars, making it
  somewhat less useful than it could be
*/
/*TODO:
  tidy up formatting (e.g. trailing \)
  ensure that split(" /") does not miss anything
  verify that everything is sorted alphabetically by pinyin
  implement search...
  give some thought to how we can implement search for characters/English too...
     maybe create some sort of hashmap
  search should not require exact matches, if whole provided string is a substring of pinyin
  should be something like:
    if it's a (partial) match:
      traverse the list both backwards and forwards as far as possible and add all the matches
  (starting at same place), this should be a match too.
  also,** multiple words have same pinyin** - for themoment, this will return only the first result
  
  could probably just switch to using an array too...
  
  Capitals are causing issues too...
*/
public class Extract{
  private static final String DEFAULT_DICTIONARY_FILENAME="dict";

  public List<Word> dictionary = new ArrayList<Word>();

  public void preProcess(){
    //will want to uncomment the first 31 lines code for this :/
    for (Word w:dictionary) System.out.println(w.getSpecialOutput());
    //then run the following on this output:| sort | cut -d ' ' -f2- > dict

  }

  //may want to be a different type
  public List<Word> findMatches(int index, String pinyin){
    //go back and forth until no matches, storing/printing all the matches as you go along
    List<Word> matches = new ArrayList<Word>();
    matches.add(dictionary.get(index));
    //traverse in reverse order first
    for (int i=index-1;i >0;i--){
      Word tmp = dictionary.get(i);
      if(tmp.getPinyinNoTones().startsWith(pinyin)){
        matches.add(tmp);
      } else {
        break;
      }
    }

    //then forwards
    for (int i=index+1;i<dictionary.size();i++){
      Word tmp = dictionary.get(i);
      if(tmp.getPinyinNoTones().startsWith(pinyin)){
        matches.add(tmp);
      } else {
        break;
      }
    }
    return matches;
  }

  public List<Word> getWordPin(String pinyin){
    int left=0;
    int right=dictionary.size() -1;
    do{
      int mid = left + (right - left) /2;

      Word midWord=dictionary.get(mid);
      String midPin = midWord.getPinyinNoTones();
      if(midPin.startsWith(pinyin)){ //that is compare ==0 (more or less...)
        return findMatches(mid,pinyin);
      } else if(pinyin.compareTo(midPin) < 0){
        System.out.println(midWord.getPinyinWithTones() + ":<0");
        right = mid - 1;
      }else if (pinyin.compareTo(midPin) > 0){
        System.out.println(midWord.getPinyinWithTones() + ":>0");
        left = mid + 1;//make more efficient by only running compareTo once
      } /*else {
        return midWord;
      }*/
    } while (left <= right);
    //if not found:
    //Word empty = new Word();
    //empty.trad = empty.simp=empty.pinyin=empty.pinTones=empty.def="The word was not found";
    return null;
  }

  private static List<Word> readInDictionary(String filename){
    List<Word> wordList=new ArrayList<Word>();
    try (BufferedReader br = new BufferedReader(new FileReader(new File(filename)))) {
      String line;
      while ((line = br.readLine()) != null) {
        Word word = new Word();
        String[] str=line.split(" /");
        word.setDefinition(str[1]);
        String[] rem=str[0].split("\\[");
        word.setPinyinNoTones(rem[1].replaceAll("[\\[\\]12345 ]", "").toLowerCase());
        word.setPinyinWithTones(rem[1].replaceAll("[\\[\\]]","").toLowerCase());

        String[] remRem=rem[0].split(" ");
        word.setTraditionalChinese(remRem[0]);
        word.setSimplifiedChinese(remRem[1]);
        wordList.add(word);
      }
    } catch (Exception e){
      e.printStackTrace();
    }
    return wordList;
  }

  public Word getWordFromChinese(char c){
    return getWordFromChinese(""+c);
  }

  public Word getWordFromChinese(String chineseWord){
    for (Word word : dictionary){
      if(word.getSimplifiedChinese().equals(chineseWord)
                      || word.getTraditionalChinese().equals(chineseWord)){
        return word;
      }
    }
    return null;
  }

  public String getEnglish(String chineseWord){
    for (Word word : dictionary){
      if(word.getSimplifiedChinese().equals(chineseWord)
                      || word.getTraditionalChinese().equals(chineseWord)){
        return word.getDefinition();
      }
    }
    return "Chinese word not found";
  }

  public String getPinyinWithTones(String chineseWord){
    for (Word word : dictionary){
      if(word.getSimplifiedChinese().equals(chineseWord)
                      || word.getTraditionalChinese().equals(chineseWord)){
        return word.getPinyinWithTones();
      }
    }
    return "Chinese word not found";
  }
  public Extract(){
    dictionary=readInDictionary(DEFAULT_DICTIONARY_FILENAME);
  }
  
}
