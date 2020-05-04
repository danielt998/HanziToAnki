import java.util.ArrayList;
import java.util.List;


public class Deck {
    private List<String> lines = new ArrayList<String>();

    //USE ONLY FOR TESTING
    public static void main(String[] args) {
        Deck deck = new Deck();
        deck.addLine("This is a line");
        deck.addLine("This is another line, aEIORxfi;lhmgzfAD," + "another");
        List<String> tmp = deck.getLines();
        for (String line : tmp) {
            System.out.println(line);
        }
    }

    public void addLine(String line) {
        lines.add(line);
    }

    public List<String> getLines() {
        return lines;
    }
}
