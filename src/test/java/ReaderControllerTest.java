import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import server.controller.ReaderController;

class ReaderControllerTest {

    private final ReaderController controller = new ReaderController();

    @Test
    void segmentsChineseTextIntoDictionaryEntries() {
        var response = controller.segment("我喜欢学习中文。");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertFalse(response.getBody().isEmpty());
        assertTrue(response.getBody().stream()
                .map(ReaderController.ReaderToken::dictionary)
                .filter(java.util.Objects::nonNull)
                .anyMatch(word -> word.simplified().equals("中文")));
        assertTrue(response.getBody().stream()
                .map(ReaderController.ReaderToken::dictionary)
                .filter(java.util.Objects::nonNull)
                .anyMatch(word -> word.hskLevel() != null));
    }

    @Test
    void exportsSelectedWordsAsAnkiTsv() {
        var response = controller.export(
                List.of("中文", "学习"),
                List.of("我喜欢中文。", "我喜欢学习。"),
                0,
                "SIMP",
                "ANKI",
                "INDEX_CARD_3x5",
                true);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("attachment; filename=reader-flashcards.tsv",
                response.getHeaders().getFirst("Content-Disposition"));
        String cards = new String(response.getBody(), StandardCharsets.UTF_8);
        assertTrue(cards.contains("中文"));
        assertTrue(cards.contains("我喜欢中文。"));
    }

    @Test
    void rejectsInvalidReaderExportSettings() {
        var response = controller.export(
                List.of("中文"), List.of(), 7, "SIMP", "ANKI", "INDEX_CARD_3x5", true);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void exportsSelectedWordsAsPdfFlashcards() {
        var response = controller.export(
                List.of("中文"),
                List.of("我喜欢中文。"),
                0,
                "SIMP",
                "PDF_FLASHCARDS",
                "BUSINESS_CARD",
                false);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("application/pdf", response.getHeaders().getContentType().toString());
        assertTrue(new String(response.getBody(), 0, 4, StandardCharsets.US_ASCII).startsWith("%PDF"));
    }
}
