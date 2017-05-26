import java.io.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
/*TODO:
 *  move more of the code to use HashSets, some of it still allows duplicates
 *  move stuff out of the main method and find some way to make it a little more generic
 */
public class Main{
  public static char[] getCharsFromFile(String fileName){
    String acc="";//TODO:StringBuilder?
    try{
      FileReader fileReader = new FileReader(fileName);
      BufferedReader bufferedReader = new BufferedReader(fileReader);
      String tmpLine;
      while((tmpLine = bufferedReader.readLine())!=null){
        acc += tmpLine;
       }
    }catch(Exception e){}
    finally{}
    char[] allChars = acc.toCharArray();
    String chineseCharsOnly = "";
    for (char c:allChars){
      if(Character.UnicodeScript.of(c)==Character.UnicodeScript.HAN){
        chineseCharsOnly += c;
      }
    } 
    return chineseCharsOnly.toCharArray();
  }

  private static String[] getWordsFromFile(String filename,boolean removeDupes){
    List<String> lines = new ArrayList<String>();
    try{
      Scanner sc = new Scanner(new File(filename));
outerLoop:
      while (sc.hasNextLine()) {
        String nextLine=sc.nextLine();
        if(removeDupes){
          for(String line:lines){
            if(line.equals(nextLine)){
              continue outerLoop;
            }
          }
        }
        lines.add(nextLine);
      }
    }catch(Exception e){}
    return lines.toArray(new String[0]);
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
               "\t-w --word-list:\tRead from an input file containing a list of words, separated"+
               " by line breaks. Without this flag, individual characters are extracted.");
/*    System.out.println("\t-a --all-words:\tLooks up all possible two and three letter combinations"
                    + " and returns all those that match. This may later become default behaviour."
                    + " Note that this cannot be used in conjunction with the -w flag as this would"
                    + " be nonsensical.");//variable number of max char look aheads is also possible
                                         //and min? e.g. 1
*/
    System.out.println("\t-s --single-characters:\tExtract only single characters from the file.");
  }
  
  public static void writeStringListToFile(List<String> list, String outputFileName){
    BufferedWriter outputWriter=null;
    try{
      outputWriter=new BufferedWriter(new FileWriter(outputFileName));
      for(String line:list){
        outputWriter.write(line+"\n");
      }
    }catch(IOException e){
      e.printStackTrace();
    }finally{
      try{
        outputWriter.close();
      }catch(IOException e){
        e.printStackTrace();
      }
    }
  }

  public static void produceDeck(String[] lines, boolean useWordList, boolean allWords,
                                                                    String outputFileName){
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
  }

  public void produceDeck(String filename, boolean useWordList, boolean allWords, String outputFileName){

  }

  public static void main(String[] args){
    if(args.length==0||args[0].equals("-h")){
      printUsage();
      return;
    }
    boolean useWordList=false;
    boolean allWords=true;
    String filename = args[args.length-1];
    for(int argno=0;argno<args.length-1;argno++){
      //flag handling
      if(args[argno].equals("-w")||args[argno].equals("--word-list")){
        useWordList=true;
        allWords=false;
      }else if(args[argno].equals("-s")||args[argno].equals("--single-characters")){
        allWords=false;
        useWordList=false;
      }else{
        System.out.println(
                       "An unrecognised flag was used, please see below for usage information:\n");
        printUsage();
        return;
      }
      //handle other flags..., create a separate class if args get too numerous
    }

    List<String> outputStringList;
    if(useWordList){
      outputStringList=getAnkiOutputWordList(filename);
    }else if(allWords){
      outputStringList=getAnkiOutputForOneTwoThreeCharWords(filename);
    }else{
      outputStringList=getAnkiOutputFromSingleChars(filename);
    }

    for(String s:outputStringList){
      System.out.println(s);
    }
  }

  private static List<String> getAnkiOutputWordList(String filename){
    List<String> output=new ArrayList<String>();
    String[] stringArr=getWordsFromFile(filename,true);
    return getAnkiOutputWordListFromStringArr(stringArr);
  }

  private static List<String> getAnkiOutputWordListFromStringArr(String[] stringArr){
    Extract extract = new Extract();
    List<String> output=new ArrayList<String>();
    Set<Word> words= new HashSet<Word>();
    for(int i=0;i<stringArr.length;i++){
      String s=stringArr[i];
      words.add(extract.getWordFromChinese(s));
//      output.add(i+";"+s + ";" + extract.getEnglish(s)
//                                       .replaceAll(";",",")+"-"+extract.getPinyinWithTones(s));
    }
    return DeckFactory.generateDeck(words).getLines();
    //return output;
  }

  private static List<String> getAnkiOutputForOneTwoThreeCharWords(String filename){
      char[] charArray=getCharsFromFile(filename);
      return getAnkiOutputForOneTwoThreeCharWordsWithCharArr(charArray);
  }
  private static List<String> getAnkiOutputForOneTwoThreeCharWordsWithCharArr(char[]  charArray){
      List<String> output=new ArrayList<String>();
      //should be similar to below, but reads ahead one and two characters
      //also need to make sure that is nothing is found, nothing is returned
      //For now, this will not return single characters if they can exist as part of any 'word'
      Extract extract = new Extract();
      Set<Word> words=new HashSet<Word>();
      for(int i=0;i<charArray.length;i++){
        String word=""+charArray[i];
        boolean wordUsed=false;
        if(i+1<charArray.length){
          Word wordTwoChars= extract.getWordFromChinese(word + charArray[i+1]);
          if(wordTwoChars!=null){        
            words.add(wordTwoChars);
            wordUsed=true;
            i++;
          }
        }
        if(i+2-1<charArray.length){
          Word wordThreeChars= extract.getWordFromChinese(word+charArray[i+1-1]+charArray[i+2-1]);
          if(wordThreeChars!=null){
            words.add(wordThreeChars);
            wordUsed=true;
            i++;
          }
        }
        if(!wordUsed){//iff character is not used as part of any other word, we print it
          //TODO:onsider whether this should be the behaviour and how arguments might be restructured
          Word wordSingleChar=extract.getWordFromChinese(word);
          if(wordSingleChar!=null){
            words.add(wordSingleChar);
          }
        }
      }
      int i=0;
      output=DeckFactory.generateDeck(words).getLines();
      /*
      for(Word word:words){
        output.add(i++ +";"+ word.getSimplifiedChinese() + ";" +word.getPinyinWithTones()+" - "
                                                  + word.getDefinition().replaceAll(";",","));
      }*/
    return output;
  }

  private static List<String> getAnkiOutputFromSingleCharsWithCharArr(char[] charArray){
    Extract extract = new Extract();
    Set<Word> words=new HashSet<Word>();
    //List<String> output=new ArrayList<String>();
    for(int i=0;i<charArray.length;i++){
      char c=charArray[i];
      Word word=extract.getWordFromChinese(c);
      words.add(word);
/*      output.add(i+";"+word.getSimplifiedChinese() + ";" +word.getPinyinWithTones()
                                + " - " + word.getDefinition().replaceAll(";",","));
                                */
    }
    return DeckFactory.generateDeck(words).getLines();
    //return output;
  }

  private static List<String> getAnkiOutputFromSingleChars(String filename){
      char[] charArray=getCharsFromFile(filename);
      return getAnkiOutputFromSingleCharsWithCharArr(charArray);
  }
}
