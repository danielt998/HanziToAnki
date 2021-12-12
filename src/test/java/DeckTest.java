import hanziToAnki.Deck;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import java.util.List;

class DeckTest {

    @Test
    void readsLinesFromFile() {
        Deck deck = new Deck();
        deck.addLine("This is a line");
        deck.addLine("This is another line, aEIORxfi;lhmgzfAD," + "another");

        List<String> lines = deck.getLines();
        Assertions.assertEquals(2, lines.size());
    }
}