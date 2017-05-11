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

  /*last argument is file name, others are all modifiers:
   * -f:file containing words separated by spaces, otherwise defaults to individual chars
   */
  public static void printUsage(){
    System.out.println("Usage: java ConvertToCSV [-f] filename");
    System.out.println("arguments:");
    System.out.println("\t-f:\tRead from an input file containing a list of words, separated"+
                        " by line breaks. Without this flag, individual characters are extracted.");
  }
  
  public static void main(String[] args){
    if(args.length==0||args[0].equals("-h")){
      printUsage();
      return;
    }
    boolean useWordList=false;
    String filename = args[args.length-1];
    for(int argno=0;argno<args.length-1;argno++){
      //flag handling
      if(args[argno].equals("-f")){
        useWordList=true;
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
