package server.controller;

import hanziToAnki.CardStyle;
import hanziToAnki.DeckProducer;
import hanziToAnki.DictionaryExtractor;
import hanziToAnki.ExportOptions;
import hanziToAnki.FileUtils;
import hanziToAnki.OutputFormat;
import hanziToAnki.chinese.ChineseDeckStyler;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import hanziToAnki.chinese.ChineseWordFinder;
import java.io.File;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import utils.TemporaryDirectory;


@RestController
public class DeckGeneratorController {

    @PostMapping(value = "/generate", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<byte[]> generate(
            @RequestParam(value = "uploadFile", required = false) MultipartFile uploadFile,
            @RequestParam(value = "textInput", required = false) String textInput,
            @RequestParam(value = "strategy", defaultValue = "6") int strategyIndex,
            @RequestParam(value = "hskLevel", defaultValue = "0") int hskLevel,
            @RequestParam(value = "hanziType", defaultValue = "SIMP") String hanziTypeStr,
            @RequestParam(value = "format", defaultValue = "ANKI") String formatStr,
            @RequestParam(value = "cardStyle", defaultValue = "INDEX_CARD_3x5") String cardStyleStr,
            @RequestParam(value = "toneColors", defaultValue = "true") boolean toneColors
    ) throws IOException {

        // Validate that either file or text is provided
        if ((uploadFile == null || uploadFile.isEmpty()) && (textInput == null || textInput.trim().isEmpty())) {
            return ResponseEntity.badRequest().build();
        }

        // Map strategy index to enum
        ChineseWordFinder.STRATEGY strategy = switch (strategyIndex) {
            case 0 -> ChineseWordFinder.STRATEGY.TRI_BI_MONOGRAMS_USE_ALL_CHARS_BIGRAM_OVERLAP;
            case 1 -> ChineseWordFinder.STRATEGY.TRI_BI_MONOGRAMS_USE_ALL_CHARS;
            case 2 -> ChineseWordFinder.STRATEGY.BIGRAM_AND_MONOGRAM_ONLY_NO_OVERLAP;
            case 3 -> ChineseWordFinder.STRATEGY.BIGRAM_AND_MONOGRAM_ONLY_OVERLAP;
            case 4 -> ChineseWordFinder.STRATEGY.SINGLE_CHAR_ONLY;
            case 5 -> ChineseWordFinder.STRATEGY.ALL_COMBINATIONS;
            case 6 -> ChineseWordFinder.STRATEGY.ANSJ_SEGMENTATION;
            default -> ChineseWordFinder.STRATEGY.ANSJ_SEGMENTATION;
        };

        OutputFormat outputFormat = OutputFormat.valueOf(formatStr);
        ChineseDeckStyler.HanziType hanziType = ChineseDeckStyler.HanziType.valueOf(hanziTypeStr);

        // useWordList=false to extract words from text (not treat lines as whole words)
        ExportOptions options = new ExportOptions(false, true, hskLevel, strategy, outputFormat, hanziType);

        try (TemporaryDirectory tempDirectory = new TemporaryDirectory()) {

            DictionaryExtractor extractor = new ChineseDictionaryExtractor();
            extractor.readInDictionary();

            DeckProducer deckProducer = new DeckProducer(extractor);

            File inputFile = (uploadFile != null && !uploadFile.isEmpty()) 
                    ? tempDirectory.getFileFromMultipart(uploadFile)
                    : tempDirectory.getFileFromText(textInput);
            File flashcardFile = tempDirectory.getFile();

            // Parse card style
            CardStyle cardStyle;
            try {
                cardStyle = CardStyle.valueOf(cardStyleStr);
            } catch (IllegalArgumentException e) {
                cardStyle = CardStyle.INDEX_CARD_3x5;
            }

            // Handle different output formats
            byte[] fileContent;
            String fileExtension;
            String contentType;
            
            if (outputFormat == OutputFormat.PDF_FLASHCARDS) {
                // Generate PDF flashcards
                List<String> inputLines = FileUtils.fileToStringArray(inputFile.getAbsolutePath());
                fileContent = deckProducer.producePdfFlashcards(inputLines, options, cardStyle, toneColors);
                fileExtension = "pdf";
                contentType = "application/pdf";
            } else {
                // Generate text-based formats (ANKI, PLECO, MEMRISE)
                List<String> outputLines = deckProducer.produceDeck(
                        inputFile.getAbsolutePath(),
                        options
                );
                FileUtils.writeToFile(outputLines, flashcardFile.getAbsolutePath());
                fileContent = Files.readAllBytes(flashcardFile.toPath());
                fileExtension = "tsv";
                contentType = "text/plain";
            }

            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.parseMediaType(contentType));
            header.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + "flashcards." + fileExtension);
            header.setContentLength(fileContent.length);

            return new ResponseEntity<>(fileContent, header, HttpStatus.OK);
        }
    }
}