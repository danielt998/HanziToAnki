package server.controller;

import hanziToAnki.DeckProducer;
import hanziToAnki.DictionaryExtractor;
import hanziToAnki.ExportOptions;
import hanziToAnki.FileUtils;
import hanziToAnki.OutputFormat;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import java.io.IOException;
import java.net.URISyntaxException;
import org.springframework.core.io.FileSystemResource;
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

    //https://github.com/IdiosApps/gradedReaderBuilderServer/blob/master/src/main/java/com/idiosApps/gradedReaderBuilderServer/BuildController.java
    @PostMapping(value = "/generate", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<FileSystemResource> generate(@RequestParam("uploadFile") MultipartFile uploadFile
//                         @RequestParam("outputType") String outputType,
    ) throws IOException, URISyntaxException {

        // TODO get from UI
        var options = new ExportOptions(true, true, 0, OutputFormat.ANKI);

        try (TemporaryDirectory tempDirectory = new TemporaryDirectory()) {

            // TODO use language radio button / detect language, to choose relevant dictionary
            DictionaryExtractor extractor = new ChineseDictionaryExtractor();
            extractor.readInDictionary();

            DeckProducer deckProducer = new DeckProducer(extractor);

            var localFile = tempDirectory.getFileFromMultipart(uploadFile);
            var flashcardFile = tempDirectory.getFile();

            var outputLines = deckProducer.produceDeck(
                    localFile.getAbsolutePath(),
                    options
            );
            FileUtils.writeToFile(outputLines, flashcardFile.getAbsolutePath());

            HttpHeaders header = new HttpHeaders();
            header.setContentType(MediaType.TEXT_PLAIN);
            header.set(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=" + "flashcards.anki");
            header.setContentLength(flashcardFile.length());

            return new ResponseEntity(new FileSystemResource(flashcardFile), header, HttpStatus.OK);
        }
    }
}