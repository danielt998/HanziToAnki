public class ConvertToCSV{
  public static char[] getChars(){
    //could read from a file, or pass in this String(or other type) from constructor
    return "这是一些汉字".toCharArray();
  }
  public static void main(String[] args){
    Extract extract = new Extract();
    for(char c:getChars()){
      System.out.println("Char:" + c + "English:" + extract.getEnglish(""+c));
    }
  }

}
