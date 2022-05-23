import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.tomcat.util.http.fileupload.FileUpload;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;
import server.Application;
import server.controller.DeckGeneratorController;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

// inspired by https://stackoverflow.com/questions/45565125/how-to-test-file-upload-in-spring-boot
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class GeneratorControllerTest {

    private MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    public void setUp() {
        mockMvc = webAppContextSetup(webApplicationContext).build();
    }

    @Test
    public void canUploadAWord() throws Exception {
        var inputText = "爱";
        var file = new MockMultipartFile("uploadFile",
                "aWord.txt",
                "text/plain",
                inputText.getBytes(StandardCharsets.UTF_8)
        );

        MvcResult result = mockMvc
                .perform(multipart("/generate", file))
                .andExpect(status().isOk())
                .andReturn();

        int one = 1;
    }
}
