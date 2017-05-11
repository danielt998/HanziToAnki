import java.io.*;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
public class ConvertToCSV{
  /*todo:
    obviously any semicolons will be lost, is this an issue?
  */
  public static char[] getChars(){
    //could read from a file, or pass in this String(or other type) from constructor
    return "这是一些汉字".toCharArray();
  }
  public static char[] getCharsFromFile(String fileName){
    String acc="";//StringBuilder?
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

  private static String[] getWordsFromFile(String filename){
    List<String> lines = new ArrayList<String>();
    try{
      Scanner sc = new Scanner(new File(filename));
      while (sc.hasNextLine()) {
              lines.add(sc.nextLine());
      }
    }catch(Exception e){}
    return lines.toArray(new String[0]);
  }

  public static void printUsage(){
    System.out.println("Usage: java ConvertToCSV [-f] [-w] filename");
    System.out.println("arguments:");
    System.out.println("\t-w --word-list:\tRead from an input file containing a list of words, separated"+
                        " by line breaks. Without this flag, individual characters are extracted.");
    System.out.println("\t-a --all-words:\tLooks up all possible two and three letter combinations "
                    + "and returns all those that match. This may later become default behaviour."
                    + "Note that this cannot be used in conjunction with the -w flag as this would"
                    + "be nonsensical.");//variable number of max char look aheads is also possible
                                         //and min? e.g. 1
  }
  
  public static void main(String[] args){
    if(args.length==0||args[0].equals("-h")){
      printUsage();
      return;
    }
    boolean useWordList=false;
    boolean allWords=false;
    String filename = args[args.length-1];
    for(int argno=0;argno<args.length-1;argno++){
      //flag handling
      if(args[argno].equals("-w")||args[argno].equals("--word-list")){
        useWordList=true;
      }else if(args[argno].equals("-a")||args[argno].equals("--all-words")){
        allWords=true;
      }else{
        System.out.println(
                       "An unrecognised flag was used, please see below for usage information:\n");
        printUsage();
        return;
      }
      //handle other flags..., create a separate class if args get too numerous
    }

    if(useWordList){
      Extract extract = new Extract();
      String[] stringArr=getWordsFromFile(filename);
      for(int i=0;i<stringArr.length;i++){
        String s=stringArr[i];
        System.out.println(i+";"+s + ";" + extract.getEnglish(s)
                                                  .replaceAll(";",",")+"-"+extract.getPinyinWithTones(s));

      }
    }else if(allWords){
      //TODO
      //should be similar to below, but reads ahead one and two characters
      //also need to make sure that is nothing is found, nothing is returned
      //For now, this will not return single characters if they can exist as part of any 'word'
      Extract extract = new Extract();
      char[] charArray=getCharsFromFile(filename);
      for(int i=0;i<charArray.length;i++){
        String word=""+charArray[i];
        boolean wordUsed=false;
        if(i+1<charArray.length){
          String wordTwoChars= word + charArray[i+1];
          if(!extract.getEnglish(wordTwoChars).equals("Chinese word not found")){        
            System.out.println(i+";"+ wordTwoChars + ";" + extract.getEnglish(wordTwoChars)
                                                  .replaceAll(";",","));
            wordUsed=true;
          }
        }
        if(i+2<charArray.length){
          String wordThreeChars= word + charArray[i+1] + charArray[i+2];
          if(!extract.getEnglish(wordThreeChars).equals("Chinese word not found")){        
            System.out.println(i+";"+ wordThreeChars + ";" + extract.getEnglish(wordThreeChars)
                                                  .replaceAll(";",","));
            wordUsed=true;
            i+=2;
          }
        }else if(wordUsed){ //that is, a two char word exists
          i++;
        }

        if(!wordUsed){//iff character is not used as part of any other word, we print it
          //TODO:onsider whether this should be the behaviour and how arguments might be restructured
          System.out.println(i+";"+ word + ";" + extract.getEnglish(word)
                                                  .replaceAll(";",","));
        }
      }

    }else{
      Extract extract = new Extract();
      //char[] charArray=getChars();
      char[] charArray=getCharsFromFile(filename);
      for(int i=0;i<charArray.length;i++){
        char c=charArray[i];
        System.out.println(i+";"+c + ";" + extract.getEnglish(""+c)
                                                  .replaceAll(";",","));
      }
    }//else
  }
}
