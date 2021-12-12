package hanziToAnki;

import java.util.ArrayList;
import java.util.List;


public class Deck {
    private final List<String> lines = new ArrayList<>();

    public void addLine(String line) {
        lines.add(line);
    }

    public List<String> getLines() {
        return lines;
    }
}
