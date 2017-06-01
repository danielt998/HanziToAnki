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
  private static char[] getCharsFromList(List<String> lines){
    StringBuilder fullString=new StringBuilder("");
    for(String line: lines){
      fullString.append(line);
    }
    char[] allChars = fullString.toString().toCharArray();
    StringBuilder chineseCharsOnly = new StringBuilder("");
    for (char c:allChars){
      if(Character.UnicodeScript.of(c)==Character.UnicodeScript.HAN){
        chineseCharsOnly.append(c);
      }
    }
    return chineseCharsOnly.toString().toCharArray();
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

  public static void produceDeck(List<String> lines, Options options, String outputFileName){
      Set<Word> words = new HashSet();
      if(options.getUseWordList()){
          words.addAll(VocabularyImporter.getWordsFromStringList(lines));
      }else if(options.getAllWords()){
        words.addAll(getAnkiOutputForOneTwoThreeCharWords(lines));
      }else{
          words.addAll(getAnkiOutputFromSingleChars(lines));
      }
      words.removeAll(VocabularyImporter.getAccumulativeHSKVocabulary(options.getHskLevelToExclude()));
      List<String> outputLines=DeckFactory.generateDeck(words).getLines();
      FileUtils.writeToFile(outputLines,outputFileName);
  }

  public static void produceDeck(String filename, Options options,String outputFileName){
    produceDeck(FileUtils.fileToStringArray(filename),options,outputFileName);
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
    //TODO: provide a command line option for the user to override this name
    String outputFileName=FileUtils.removeExtensionFromFileName(filename)+".csv";
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
    }//for
    produceDeck(filename,new Options(useWordList,allWords,hskLevelToExtract),outputFileName);
  }

  private static Set<Word> getAnkiOutputForOneTwoThreeCharWords(List<String> list){
      char[] charArray=getCharsFromList(list);
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
          //TODO:consider whether this should be the behaviour and how arguments might be restructured
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
  private static Set<Word> getAnkiOutputFromSingleChars(List<String> lines){
    char[] charArray =getCharsFromList(lines);
    return getAnkiOutputFromSingleCharsWithCharArr(charArray);
  }
}
