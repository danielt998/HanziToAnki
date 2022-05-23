package server.controller;

import hanziToAnki.*;
import hanziToAnki.chinese.ChineseDictionaryExtractor;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import utils.TemporaryDirectory;
import utils.TemporaryFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

@RestController
public class DeckGeneratorController {

    //https://github.com/IdiosApps/gradedReaderBuilderServer/blob/master/src/main/java/com/idiosApps/gradedReaderBuilderServer/BuildController.java
    @PostMapping("/generate")
    public void generate(@RequestParam("uploadFile") MultipartFile uploadFile,
//                         @RequestParam("outputType") String outputType,
                         HttpServletResponse response) throws IOException, URISyntaxException {

        // TODO get from UI
        var options = new ExportOptions(true, true, 0, OutputFormat.ANKI);

        try (TemporaryDirectory tempDirectory = new TemporaryDirectory()) {

            // TODO use language radio button / detect language, to choose relevant dictionary
            DictionaryExtractor extractor = new ChineseDictionaryExtractor();
            extractor.readInDictionary();

            DeckProducer deckProducer = new DeckProducer(extractor);

            var localFile = tempDirectory.getFileFromMultipart(uploadFile);
            var outputFile = tempDirectory.getFile(".anki");

            var outputLines = deckProducer.produceDeck(
                    localFile.getAbsolutePath(),
                    options
            );
            FileUtils.writeToFile(outputLines, outputFile.getAbsolutePath());

            IOUtils.copy(new FileInputStream(outputFile), response.getOutputStream());
            response.flushBuffer();


            String contentType = Files.probeContentType(outputFile.toPath());
            response.setContentType(contentType);
        }
    }
}