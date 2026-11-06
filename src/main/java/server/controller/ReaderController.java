package server.controller;

import hanziToAnki.CardStyle;
import hanziToAnki.DeckProducer;
import hanziToAnki.ExportOptions;
import hanziToAnki.OutputFormat;
import hanziToAnki.Word;
import hanziToAnki.chinese.ChineseDeckStyler;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import hanziToAnki.chinese.ChineseGrader;
import hanziToAnki.chinese.ChineseWord;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
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
        List<Set<Word>> hskVocabularyByLevel = getHskVocabularyByLevel(extractor);
        List<ReaderToken> tokens = new ArrayList<>();
        for (Term term : ToAnalysis.parse(text).getTerms()) {
            tokens.add(new ReaderToken(
                    term.getName(),
                    term.getOffe(),
                    term.getOffe() + term.getName().length(),
                    lookUpWord(extractor, term.getName(), hskVocabularyByLevel)));
        }
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/reader/export")
    public ResponseEntity<byte[]> export(
            @RequestParam List<String> words,
            @RequestParam(required = false) List<String> contexts,
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
        List<ReaderSelection> selections = getSelections(words, contexts, extractor);
        if (selections.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        ExportOptions options = new ExportOptions(
                true, true, hskLevel, null, format, hanziType);
        DeckProducer deckProducer = new DeckProducer(extractor);
        byte[] content;
        MediaType contentType;
        String filename;
        if (format == OutputFormat.PDF_FLASHCARDS) {
            content = deckProducer.producePdfFlashcards(
                    selections.stream().map(ReaderSelection::word).toList(), options, cardStyle, toneColors);
            contentType = MediaType.APPLICATION_PDF;
            filename = "reader-flashcards.pdf";
        } else {
            List<String> cards = produceAnkiCards(deckProducer, selections, options, hanziType);
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

    private List<ReaderSelection> getSelections(
            List<String> words, List<String> contexts, ChineseDictionaryExtractor extractor) {
        List<String> selectionContexts = contexts == null ? Collections.emptyList() : contexts;
        List<ReaderSelection> selections = new ArrayList<>();
        for (int index = 0; index < words.size(); index++) {
            String word = words.get(index);
            if (extractor.getWord(word).isPresent()) {
                String context = index < selectionContexts.size() ? selectionContexts.get(index) : "";
                selections.add(new ReaderSelection(word, context));
            }
        }
        return selections;
    }

    private List<String> produceAnkiCards(
            DeckProducer deckProducer,
            List<ReaderSelection> selections,
            ExportOptions options,
            ChineseDeckStyler.HanziType hanziType) {
        List<String> cards = new ArrayList<>();
        for (ReaderSelection selection : selections) {
            List<String> styledCard = deckProducer.produceDeck(List.of(selection.word()), options);
            if (styledCard.size() < 2) {
                continue;
            }
            if (cards.isEmpty()) {
                cards.add(styledCard.getFirst());
            }
            cards.add(addContext(styledCard.get(1), selection.context(), hanziType));
        }
        return cards;
    }

    private String addContext(String card, String context, ChineseDeckStyler.HanziType hanziType) {
        if (context == null || context.isBlank()) {
            return card;
        }
        String[] fields = card.split("\t", -1);
        int definitionIndex = hanziType == ChineseDeckStyler.HanziType.SIMP_AND_TRAD ? 2 : 1;
        fields[definitionIndex] += "<br><br><span class=\"sentence-context\">"
                + escapeHtml(context) + "</span>";
        return String.join("\t", fields);
    }

    private String escapeHtml(String text) {
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private List<Set<Word>> getHskVocabularyByLevel(ChineseDictionaryExtractor extractor) {
        ChineseGrader grader = new ChineseGrader(extractor);
        return IntStream.rangeClosed(1, 6)
                .mapToObj(grader::getAccumulativeVocabulary)
                .toList();
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

    private ReaderWord lookUpWord(
            ChineseDictionaryExtractor extractor, String text, List<Set<Word>> hskVocabularyByLevel) {
        if (!isChineseOnly(text)) {
            return null;
        }
        return extractor.getWord(text)
                .filter(ChineseWord.class::isInstance)
                .map(ChineseWord.class::cast)
                .map(word -> new ReaderWord(
                        word.simplified(),
                        word.traditional(),
                        word.pinyinTones(),
                        word.definition(),
                        getHskLevel(word, hskVocabularyByLevel)))
                .orElse(null);
    }

    private Integer getHskLevel(Word word, List<Set<Word>> hskVocabularyByLevel) {
        for (int index = 0; index < hskVocabularyByLevel.size(); index++) {
            if (hskVocabularyByLevel.get(index).contains(word)) {
                return index + 1;
            }
        }
        return null;
    }

    public record ReaderToken(String text, int start, int end, ReaderWord dictionary) {
    }

    public record ReaderWord(
            String simplified, String traditional, String pinyin, String definition, Integer hskLevel) {
    }

    private record ReaderSelection(String word, String context) {
    }
}
