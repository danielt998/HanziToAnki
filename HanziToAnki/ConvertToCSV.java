public class ConvertToCSV{
  public static char[] getChars(){
    //could read from a file, or pass in this String(or other type) from constructor
    return "这是一些汉字".toCharArray();
  }
  public static void main(String[] args){
    Extract extract = new Extract();
    char[] charArray=getChars();
    for(int i=0;i<charArray.length;i++){
      char c=charArray[i];
      System.out.println(i+","+c + "," + extract.getEnglish(""+c)
                                                  .replaceAll(",",";"));
    }
  }

}
