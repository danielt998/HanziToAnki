import hanziToAnki.FileUtils;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FileUtilsTest {

    @Test
    void textFileToStringArray() throws URISyntaxException {
        URI uri = this.getClass().getResource("FileUtils_helloWorld.txt").toURI();
        String fileName = Path.of(uri).toString();

        List<String> strings = FileUtils.fileToStringArray(fileName);
        assertEquals(1, strings.size());
        assertEquals("hello world", strings.get(0));
    }

    @Test
    void zipFileToStringArray() throws URISyntaxException {
        URI uri = this.getClass().getResource("FileUtils_mini_cedict_1_0_ts_utf-8_mdbg.zip").toURI();
        String fileName = Path.of(uri).toString();

        List<String> strings = FileUtils.fileToStringArray(fileName);
        assertEquals(1, strings.size());
        assertEquals("# CC-CEDICT", strings.get(0));
    }

    @Test
    void gzipFileToStringArray() throws URISyntaxException {
        URI uri = this.getClass().getResource("FileUtils_mini_cedict_1_0_ts_utf-8_mdbg.txt.gz").toURI();
        String fileName = Path.of(uri).toString();

        List<String> strings = FileUtils.fileToStringArray(fileName);
        assertEquals(1, strings.size());
        assertEquals("# CC-CEDICT", strings.get(0));
    }
}