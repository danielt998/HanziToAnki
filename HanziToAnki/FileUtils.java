import java.util.List;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
public class FileUtils{
  public static List<String> fileToStringArray(String filename){
    List<String> lines = new ArrayList<String>();
    try{
      Scanner scanner = new Scanner(new File(filename));
      while(scanner.hasNextLine()){
        lines.add(scanner.nextLine());
      }
      scanner.close();
    }catch(IOException exception){
      exception.printStackTrace();
    }
    return lines;
  }
}
