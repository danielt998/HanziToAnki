package server.controller;

import hanziToAnki.DeckProducer;
import hanziToAnki.DictionaryExtractor;
import hanziToAnki.ExportOptions;
import hanziToAnki.FileUtils;
import hanziToAnki.OutputFormat;
import hanziToAnki.chinese.ChineseDeckStyler;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

import hanziToAnki.chinese.ChineseWordFinder;
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
            @RequestParam(value = "format", defaultValue = "ANKI") String formatStr
    ) throws IOException, URISyntaxException {

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
        var options = new ExportOptions(false, true, hskLevel, strategy, outputFormat, hanziType);

        try (TemporaryDirectory tempDirectory = new TemporaryDirectory()) {

            DictionaryExtractor extractor = new ChineseDictionaryExtractor();
            extractor.readInDictionary();

            DeckProducer deckProducer = new DeckProducer(extractor);

            var inputFile = (uploadFile != null && !uploadFile.isEmpty()) 
                    ? tempDirectory.getFileFromMultipart(uploadFile)
                    : tempDirectory.getFileFromText(textInput);
            var flashcardFile = tempDirectory.getFile();

            var outputLines = deckProducer.produceDeck(
                    inputFile.getAbsolutePath(),
                    options
            );
            FileUtils.writeToFile(outputLines, flashcardFile.getAbsolutePath());

            // Read file content before the try-with-resources closes the temporary directory
            byte[] fileContent = Files.readAllBytes(flashcardFile.toPath());

            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.TEXT_PLAIN);
            header.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + "flashcards.tsv");
            header.setContentLength(fileContent.length);

            return new ResponseEntity<>(fileContent, header, HttpStatus.OK);
        }
    }
}