import java.util.List;
import java.util.ArrayList;

public class Deck{
  private List<String> lines = new ArrayList<String>();
  public void addLine(String line){
    lines.add(line);
  }
  public List<String> getLines(){
    return lines;
  }

  //USE ONLY FOR TESTING
  public static void main(String[] args){
    Deck deck = new Deck();
    deck.addLine("This is a line");
    deck.addLine("This is another line, aEIORxfi;lhmgzfAD,"+"another");
    List<String> tmp=deck.getLines();
    for(String line:tmp){
      System.out.println(line);
    }
  }
}
