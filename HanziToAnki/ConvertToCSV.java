import java.io.*;
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
  public static void main(String[] args){
    Extract extract = new Extract();
    //char[] charArray=getChars();
    char[] charArray=getCharsFromFile(args[0]);
    for(int i=0;i<charArray.length;i++){
      char c=charArray[i];
      System.out.println(i+";"+c + ";" + extract.getEnglish(""+c)
                                                  .replaceAll(";",","));
    }
  }

}
