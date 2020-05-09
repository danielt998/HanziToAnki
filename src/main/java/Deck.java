import java.util.ArrayList;
import java.util.List;


public class Deck {
    private List<String> lines = new ArrayList<>();

    //USE ONLY FOR TESTING
    public static void main(String[] args) {
        Deck deck = new Deck();
        deck.addLine("This is a line");
        deck.addLine("This is another line, aEIORxfi;lhmgzfAD," + "another");
        deck.getLines().forEach(System.out::println);
    }

    public void addLine(String line) {
        lines.add(line);
    }

    public List<String> getLines() {
        return lines;
    }
}
