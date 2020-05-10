import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

class FileUtilsTest {

    @Test
    void textFileToStringArray() throws URISyntaxException {
        URI uri = this.getClass().getResource("FileUtils_helloWorld.txt").toURI();
        String fileName = Path.of(uri).toString();

        List<String> strings = FileUtils.fileToStringArray(fileName);
        Assertions.assertTrue(strings.size() == 1);
        Assertions.assertEquals("hello world", strings.get(0));
    }

    @Test
    void zipFileToStringArray() throws URISyntaxException {
        URI uri = this.getClass().getResource("FileUtils_mini_cedict_1_0_ts_utf-8_mdbg.zip").toURI();
        String fileName = Path.of(uri).toString();

        List<String> strings = FileUtils.fileToStringArray(fileName);
        Assertions.assertTrue(strings.size() == 1);
        Assertions.assertEquals("hello world", strings.get(0));
    }

    @Test
    void gzipFileToStringArray() throws URISyntaxException {
        URI uri = this.getClass().getResource("FileUtils_mini_cedict_1_0_ts_utf-8_mdbg.txt.gz").toURI();
        String fileName = Path.of(uri).toString();

        List<String> strings = FileUtils.fileToStringArray(fileName);
        Assertions.assertTrue(strings.size() == 1);
        Assertions.assertEquals("hello world", strings.get(0));
    }

    @Test
    void removeExtensionFromFileName() {
        String file = "hello.world";
        Assertions.assertEquals("hello", FileUtils.removeExtensionFromFileName(file));
    }
}