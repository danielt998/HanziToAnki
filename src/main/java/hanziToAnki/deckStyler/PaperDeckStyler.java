package hanziToAnki.deckStyler;

import hanziToAnki.ExportOptions;
import hanziToAnki.Word;

import java.util.Set;

public interface PaperDeckStyler {
    String generateImageFile(Set<Word> words, ExportOptions exportOptions);
}
