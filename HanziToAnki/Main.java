import java.io.*;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
/*TODO:
 *  move more of the code to use HashSets, some of it still allows duplicates
 *  move stuff out of the main method and find some  way to make it a little more generic
 *  in getCharsFromFile, 1) rename "acc" (accumulator), try StringBuilder chineseCharsOnly (instead of String)...
 *  ...See if StringBuilder is much faster than String for the above
 */
public class Main{
  private static char[] getCharsFromFile(String fileName){
    StringBuilder acc=new StringBuilder("");
    try{
      FileReader fileReader = new FileReader(fileName);
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      String tmpLine;
      while((tmpLine = bufferedReader.readLine())!=null){
        acc.append(tmpLine);
       }
    }catch(Exception exception){
        exception.printStackTrace();
    }
    char[] allChars = acc.toString().toCharArray();
    StringBuilder chineseCharsOnly = new StringBuilder("");
    for (char c:allChars){
      if(Character.UnicodeScript.of(c)==Character.UnicodeScript.HAN){
        chineseCharsOnly.append(c);
      }
    } 
    return chineseCharsOnly.toString().toCharArray();
  }

  private static char[] stringArrToCharArr(String[] stringArr){
    StringBuilder builder=new StringBuilder();
    for(String s : stringArr){
      builder.append(s);
    }
    return builder.toString().toCharArray();
  }

  public static void printUsage(){
    System.out.println("Usage: java Main [OPTIONS] filename");
    System.out.println("options:");
    System.out.println(
               "\t-w --word-list:\tRead from an input file containing a list of words, separated"
               + " by line breaks. Without this flag, individual characters are extracted.");
    System.out.println("\t-s --single-characters:\tExtract only single characters from the file.");
    System.out.println("\t-hsk <hsk level> Remove any words in any HSK levels up to and including"
                                                                              + " the given one.");
  }

  //TODO:resurrect this code(basically copy what is now done in main and call this from main
  public static void produceDeck(String[] lines, boolean useWordList, boolean allWords,
                                                                    String outputFileName){
         /*
   List<String> outputStringList;
    if(useWordList){
      outputStringList=getAnkiOutputWordListFromStringArr(lines);
    }else if(allWords){
      char[] charArray=stringArrToCharArr(lines);
      outputStringList=getAnkiOutputForOneTwoThreeCharWordsWithCharArr(charArray);
    }else{
      char[] charArray=stringArrToCharArr(lines);
      outputStringList=getAnkiOutputFromSingleCharsWithCharArr(charArray);
    }
    writeStringListToFile(outputStringList,outputFileName); 
    */
  }

  public void produceDeck(String filename, boolean useWordList, boolean allWords, String outputFileName){

  }

  public static void main(String[] args){
    if(args.length==0||args[0].equals("-h")||args[0].equals("-help")||args[0].equals("--help")){
      printUsage();
      return;
    }
    Extract.readInDictionary();
    boolean useWordList=false;
    boolean allWords=true;
    String filename = args[args.length-1];
    int hskLevelToExtract=0;
    for(int argno=0;argno<args.length-1;argno++){
      //flag handling
      if(args[argno].equals("-w")||args[argno].equals("--word-list")){
        useWordList=true;
        allWords=false;
      }else if(args[argno].equals("-s")||args[argno].equals("--single-characters")){
        allWords=false;
        useWordList=false;
      }else if(args[argno].equals("-hsk")){
        hskLevelToExtract=Integer.parseInt(args[++argno]);
      }else{
        System.out.println(
                       "An unrecognised flag was used, please see below for usage information:\n");
        printUsage();
        return;
      }
      //handle other flags..., create a separate class if args get too numerous
    }
    Set<Word> words = new HashSet();
    if(useWordList){
      words.addAll(VocabularyImporter.getWordsFromNewlineSeparatedFile(filename));
    }else if(allWords){
      words.addAll(getAnkiOutputForOneTwoThreeCharWords(filename));
    }else{
      words.addAll(getAnkiOutputFromSingleChars(filename));
    }
    words.removeAll(VocabularyImporter.getAccumulativeHSKVocabulary(hskLevelToExtract));
    List<String> lines=DeckFactory.generateDeck(words).getLines();
    for(String line:lines){//
      System.out.println(line);//
    }//
  }
  private static Set<Word> getAnkiOutputForOneTwoThreeCharWords(String filename){
      char[] charArray=getCharsFromFile(filename);
      return getAnkiOutputForOneTwoThreeCharWordsWithCharArr(charArray);
  }
  private static Set<Word> getAnkiOutputForOneTwoThreeCharWordsWithCharArr(char[]  charArray){
      Set<Word> words=new HashSet<Word>();
      for(int i=0;i<charArray.length;i++){
        String word=""+charArray[i];
        boolean wordUsed=false;
        if(i+1<charArray.length){
          Word wordTwoChars= Extract.getWordFromChinese(word + charArray[i+1]);
          if(wordTwoChars!=null){        
            words.add(wordTwoChars);
            wordUsed=true;
            i++;
          }
        }
        if(i+2-1<charArray.length){
          Word wordThreeChars= Extract.getWordFromChinese(word+charArray[i+1-1]+charArray[i+2-1]);
          if(wordThreeChars!=null){
            words.add(wordThreeChars);
            wordUsed=true;
            i++;
          }
        }
        if(!wordUsed){//iff character is not used as part of any other word, we print it
          //TODO:onsider whether this should be the behaviour and how arguments might be restructured
          Word wordSingleChar=Extract.getWordFromChinese(word);
          if(wordSingleChar!=null){
            words.add(wordSingleChar);
          }
        }
      }
      int i=0;
    return words;
  }

  private static Set<Word> getAnkiOutputFromSingleCharsWithCharArr(char[] charArray){
    Set<Word> words=new HashSet<Word>();
    for(char c:charArray){
      Word word=Extract.getWordFromChinese(c);
      words.add(word);
    }
    return words;
  }
  private static Set<Word> getAnkiOutputFromSingleChars(String filename){
    char[] charArray=getCharsFromFile(filename);
    return getAnkiOutputFromSingleCharsWithCharArr(charArray);
  }
}
