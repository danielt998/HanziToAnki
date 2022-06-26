import static org.junit.jupiter.api.Assertions.assertEquals;

import hanziToAnki.FileUtils;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;


@Nested
class FileUtilsTest {

    @DisplayName("Lines of text from Different types of files can be extracted")
    @ParameterizedTest(name = "Reading from {0}")
    @ValueSource(strings = {
        "FileUtils_helloWorld.txt",
        "FileUtils_mini_cedict_1_0_ts_utf-8_mdbg.zip",
        "FileUtils_mini_cedict_1_0_ts_utf-8_mdbg.txt.gz"
    })
    void readsLinesFromFile(String filename) throws URISyntaxException {
        URI uri = this.getClass().getResource(filename).toURI();
        String fileName = Path.of(uri).toString();

        List<String> lines = FileUtils.fileToStringArray(fileName);
        assertEquals(1, lines.size());
        assertEquals("# CC-CEDICT", lines.get(0));
    }

}