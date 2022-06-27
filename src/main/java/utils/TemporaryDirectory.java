package utils;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

public class TemporaryDirectory implements Closeable {
    private static final String TMP_EXT = ".tmp";
    private final Path directory;

    public TemporaryDirectory() throws IOException {
        directory = Files.createTempDirectory(null);
    }

    @Override
    public void close() throws IOException {
        directory.toFile().delete();
    }

    public TemporaryFile getFile() throws IOException {
        return getFile(TMP_EXT);
    }

    public TemporaryFile getFile(String ext) throws IOException {
        return new TemporaryFile((Files.createTempFile(directory, UUID.randomUUID().toString(), ext)));
    }

    public TemporaryFile getFileFromMultipart(MultipartFile multipartFile) throws IOException {
        TemporaryFile temporaryFile = new TemporaryFile(
                (Files.createTempFile(directory, UUID.randomUUID().toString(), TMP_EXT)));

        if (Objects.isNull(multipartFile) || multipartFile.isEmpty()) {
            return temporaryFile; // empty file (title, names) - nothing will be written to .tex file
        }

        multipartFile.transferTo(temporaryFile);
        return temporaryFile;
    }
}