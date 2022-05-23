import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import server.Application;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class GeneratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void canUploadAWord() throws Exception {
        var file = new MockMultipartFile(
                "uploadFile",
                "爱".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc
            .perform(
                multipart("/generate")
                .file(file)
                .accept(MediaType.MULTIPART_FORM_DATA)
            )
            .andExpect(status().isOk())
            .andReturn();
    }
}
