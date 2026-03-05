package com.example.samplefilestream;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.samplefilestream.file.domain.FileMetadataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "app.storage.local.base-path=build/test-storage",
    "app.storage.default-type=LOCAL"
})
class FileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @BeforeEach
    void setUp() throws Exception {
        fileMetadataRepository.deleteAll();
        Path testStoragePath = Path.of("build/test-storage");
        if (Files.exists(testStoragePath)) {
            try (var paths = Files.walk(testStoragePath)) {
                paths.sorted((a, b) -> b.compareTo(a)).forEach(path -> {
                    try {
                        Files.deleteIfExists(path);
                    } catch (Exception ignored) {
                    }
                });
            }
        }
    }

    @Test
    void uploadAndDownload_localStorage_success() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "settlement-report.csv",
            "text/csv",
            "id,amount\n1,1000".getBytes()
        );

        String uploadResponse = mockMvc.perform(multipart("/api/files").file(mockFile))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").exists())
            .andExpect(jsonPath("$.storageType").value("LOCAL"))
            .andExpect(jsonPath("$.originalFilename").value("settlement-report.csv"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        JsonNode body = objectMapper.readTree(uploadResponse);
        String id = body.get("id").asText();

        mockMvc.perform(get("/api/files/{id}/download", id))
            .andExpect(status().isOk())
            .andExpect(header().string("Content-Disposition", containsString("settlement-report.csv")))
            .andExpect(content().string("id,amount\n1,1000"));
    }

    @Test
    void upload_whenS3Disabled_thenServiceUnavailable() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
            "file",
            "a.txt",
            "text/plain",
            "hello".getBytes()
        );

        mockMvc.perform(
                multipart("/api/files")
                    .file(mockFile)
                    .param("storageType", "S3")
            )
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.code").value("STORAGE_BACKEND_UNAVAILABLE"));
    }
}
