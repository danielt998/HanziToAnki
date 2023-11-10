package hanziToAnki.deckStyler;

import hanziToAnki.ExportOptions;
import hanziToAnki.Word;

import java.util.Set;

public class EmptyPaperDeckStyler implements PaperDeckStyler{
    @Override
    public String generateImageFile(Set<Word> words, ExportOptions exportOptions) {
        return null;
    }
}
