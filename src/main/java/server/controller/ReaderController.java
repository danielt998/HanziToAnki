package server.controller;

import hanziToAnki.CardStyle;
import hanziToAnki.DeckProducer;
import hanziToAnki.ExportOptions;
import hanziToAnki.OutputFormat;
import hanziToAnki.chinese.ChineseDeckStyler;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import hanziToAnki.chinese.ChineseWord;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.ansj.domain.Term;
import org.ansj.splitWord.analysis.ToAnalysis;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReaderController {

    @PostMapping("/reader/segment")
    public ResponseEntity<List<ReaderToken>> segment(@RequestParam String text) {
        if (text.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        ChineseDictionaryExtractor extractor = createExtractor();
        List<ReaderToken> tokens = new ArrayList<>();
        for (Term term : ToAnalysis.parse(text).getTerms()) {
            tokens.add(new ReaderToken(
                    term.getName(),
                    term.getOffe(),
                    term.getOffe() + term.getName().length(),
                    lookupWord(extractor, term.getName())));
        }
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/reader/export")
    public ResponseEntity<byte[]> export(
            @RequestParam List<String> words,
            @RequestParam(defaultValue = "0") int hskLevel,
            @RequestParam(defaultValue = "SIMP") String hanziTypeValue,
            @RequestParam(defaultValue = "ANKI") String formatValue,
            @RequestParam(defaultValue = "INDEX_CARD_3x5") String cardStyleValue,
            @RequestParam(defaultValue = "true") boolean toneColors) {
        ChineseDeckStyler.HanziType hanziType = parseHanziType(hanziTypeValue);
        OutputFormat format = parseOutputFormat(formatValue);
        CardStyle cardStyle = parseCardStyle(cardStyleValue);
        if (words.isEmpty() || hskLevel < 0 || hskLevel > 6 || hanziType == null
                || format == null || cardStyle == null) {
            return ResponseEntity.badRequest().build();
        }

        ChineseDictionaryExtractor extractor = createExtractor();
        List<String> selectedWords = words.stream()
                .filter(word -> extractor.getWord(word).isPresent())
                .distinct()
                .toList();
        if (selectedWords.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ExportOptions options = new ExportOptions(
                true, true, hskLevel, null, format, hanziType);
        DeckProducer deckProducer = new DeckProducer(extractor);
        byte[] content;
        MediaType contentType;
        String filename;
        if (format == OutputFormat.PDF_FLASHCARDS) {
            content = deckProducer.producePdfFlashcards(selectedWords, options, cardStyle, toneColors);
            contentType = MediaType.APPLICATION_PDF;
            filename = "reader-flashcards.pdf";
        } else {
            List<String> cards = deckProducer.produceDeck(selectedWords, options);
            content = String.join(System.lineSeparator(), cards).getBytes(StandardCharsets.UTF_8);
            contentType = MediaType.parseMediaType("text/tab-separated-values");
            filename = "reader-flashcards.tsv";
        }

        return ResponseEntity.ok()
                .contentType(contentType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .contentLength(content.length)
                .body(content);
    }

    private ChineseDictionaryExtractor createExtractor() {
        ChineseDictionaryExtractor extractor = new ChineseDictionaryExtractor();
        extractor.readInDictionary();
        return extractor;
    }

    private ChineseDeckStyler.HanziType parseHanziType(String value) {
        return switch (value) {
            case "SIMP" -> ChineseDeckStyler.HanziType.SIMP;
            case "TRAD" -> ChineseDeckStyler.HanziType.TRAD;
            case "SIMP_AND_TRAD" -> ChineseDeckStyler.HanziType.SIMP_AND_TRAD;
            default -> null;
        };
    }

    private OutputFormat parseOutputFormat(String value) {
        return switch (value) {
            case "ANKI" -> OutputFormat.ANKI;
            case "PDF_FLASHCARDS" -> OutputFormat.PDF_FLASHCARDS;
            default -> null;
        };
    }

    private CardStyle parseCardStyle(String value) {
        return switch (value) {
            case "INDEX_CARD_3x5" -> CardStyle.INDEX_CARD_3x5;
            case "BUSINESS_CARD" -> CardStyle.BUSINESS_CARD;
            case "POSTCARD" -> CardStyle.POSTCARD;
            default -> null;
        };
    }

    private boolean isChineseOnly(String word) {
        return !word.isEmpty() && word.chars()
                .allMatch(character -> Character.UnicodeScript.of(character) == Character.UnicodeScript.HAN);
    }

    private ReaderWord lookupWord(ChineseDictionaryExtractor extractor, String text) {
        if (!isChineseOnly(text)) {
            return null;
        }
        return extractor.getWord(text)
                .filter(ChineseWord.class::isInstance)
                .map(ChineseWord.class::cast)
                .map(word -> new ReaderWord(
                        word.simplified(), word.traditional(), word.pinyinTones(), word.definition()))
                .orElse(null);
    }

    public record ReaderToken(String text, int start, int end, ReaderWord dictionary) {
    }

    public record ReaderWord(String simplified, String traditional, String pinyin, String definition) {
    }
}
