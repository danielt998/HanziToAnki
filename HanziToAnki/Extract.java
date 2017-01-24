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
  public static List<Word> list = new ArrayList<Word>();
  public static class Word{
    //yes, these should be private
    public String trad,simp,pinyin,pinTones,def;
    public String getFileOutput(){
      return trad + ' ' + simp + ' ' + '[' + pinTones + ']' + ' '+ '/' + def;
    }
    //this allows us to run the sort command to sort the data ;)
    public String getSpecialOutput(){
      return pinyin+ ' ' + trad + ' ' + simp + ' ' + '[' + pinTones + ']' + ' '+ '/' + def;
    }
  }

  public static void preProcess(){
    //will want to uncomment the first 31 lines code for this :/
    for (Word w:list) System.out.println(w.getSpecialOutput());
    //then run the following on this output:| sort | cut -d ' ' -f2- > dict

  }

  //may want to be a different type
  public static List<Word> findMatches(int index, String pinyin){
    //go back and forth until no matches, storing/printing all the matches as you go along
    List<Word> matches = new ArrayList<Word>();
    matches.add(list.get(index));
    //traverse in reverse order first
    for (int i=index-1;i >0;i--){
      Word tmp = list.get(i);
      if(tmp.pinyin.startsWith(pinyin)){
        matches.add(tmp);
      } else {
        break;
      }
    }

    //then forwards
    for (int i=index+1;i<list.size();i++){
      Word tmp = list.get(i);
      if(tmp.pinyin.startsWith(pinyin)){
        matches.add(tmp);
      } else {
        break;
      }
    }
    return matches;
  }

  public static List<Word> getWordPin(String pinyin){
    int left=0;
    int right=list.size() -1;
    do{
      int mid = left + (right - left) /2;

      Word midWord=list.get(mid);
      String midPin = midWord.pinyin;
      if(midPin.startsWith(pinyin)){ //that is compare ==0 (more or less...)
        return findMatches(mid,pinyin);
      } else if(pinyin.compareTo(midPin) < 0){
        System.out.println(midWord.pinTones + ":<0");
        right = mid - 1;
      }else if (pinyin.compareTo(midPin) > 0){
        System.out.println(midWord.pinTones + ":>0");
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
  /*TODO:Move stuff here*/
  public static void populateArray(){}
  public static char[] getChars(){
    //could read from a file, or pass in this String(or other type) from constructor
    return "这是一些汉字".toCharArray();
  }
  public static String getEnglish(String chineseWord){
    for (Word word : list){
      if(word.simp.equals(chineseWord) || word.trad.equals(chineseWord)){
        return word.def;
      }
    }
    return "Chinese word not found";
  }
  public static void main(String[] args){
      //try (BufferedReader br = new BufferedReader(new FileReader(new File("cedict_ts.u8")))) {
      try (BufferedReader br = new BufferedReader(new FileReader(new File("dict")))) {
      String line;
      for (int i=0;i<30 && (line = br.readLine()) != null;i++)
        ;//ignore first 31 lines
      while ((line = br.readLine()) != null) {
        Word word = new Word();
        String[] str=line.split(" /");
        word.def=str[1];
        String[] rem=str[0].split("\\[");
        word.pinyin=rem[1].replaceAll("[\\[\\]12345 ]", "").toLowerCase();;
        word.pinTones=rem[1].replaceAll("[\\[\\]]","").toLowerCase();//.replaceAll("\\]","");

        String[] remRem=rem[0].split(" ");
        word.trad=remRem[0];
        word.simp=remRem[1];
        list.add(word);
      }
    } catch (Exception e){
      e.printStackTrace();
    }
    //System.out.println(list.get(0).pinyin+ ":" + list.get(0).simp +":"+list.get(0).pinTones + ":" );
/*    for (Word w: getWordPin("ai")){
      System.out.println(w.pinTones);
    }
*/
    //preProcess();

    //For purposes of HanziToAnki, need the following
    for(char c:getChars()){
      System.out.println("Char:" + c + "English:" + getEnglish(""+c));
    }
  }
  
}
