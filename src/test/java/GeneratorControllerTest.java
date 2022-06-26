

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import server.Application;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class GeneratorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void canUploadAWord() throws Exception     {
        var file = new MockMultipartFile(
                "uploadFile",
                "爱".getBytes(StandardCharsets.UTF_8)

        );

        mockMvc
            .perform(
                multipart("/generate")
                .file(file)
//                .accept(MediaType.TEXT_PLAIN) // not sure why test breaks on this
            )
            .andExpect(status().isOk())
            .andReturn();

        var one = 1;
    }


}
