package com.example.samplefilemanage;

import com.example.samplefilemanage.repository.FileDownloadHistoryRepository;
import com.example.samplefilemanage.repository.StoredFileRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 파일 관리 API의 주요 업로드/다운로드 흐름을 검증하는 통합 테스트이다.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = "app.file.storage-path=build/test-storage")
class FileManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StoredFileRepository storedFileRepository;

    @Autowired
    private FileDownloadHistoryRepository fileDownloadHistoryRepository;

    @BeforeEach
    void setUp() throws IOException {
        cleanStorage();
        fileDownloadHistoryRepository.deleteAll();
        storedFileRepository.deleteAll();
    }

    @AfterEach
    void tearDown() throws IOException {
        cleanStorage();
    }

    @Test
    void uploadDownloadAndHistoryFlowWorks() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "guide.txt",
            "text/plain",
            "file-content".getBytes()
        );

        String uploadBody = mockMvc.perform(multipart("/api/files")
                .file(file)
                .param("uploadedBy", "ops-admin")
                .param("description", "운영 가이드 문서")
                .contentType(MULTIPART_FORM_DATA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileId").isNumber())
            .andExpect(jsonPath("$.originalFileName").value("guide.txt"))
            .andReturn()
            .getResponse()
            .getContentAsString();

        long fileId = JsonTestSupport.extractLong(uploadBody, "fileId");

        mockMvc.perform(get("/api/files"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].fileId").value(fileId))
            .andExpect(jsonPath("$[0].downloadCount").value(0));

        mockMvc.perform(get("/api/files/{fileId}/download", fileId)
                .param("downloadedBy", "audit-user")
                .param("reason", "감사 검토")
                .header("User-Agent", "JUnit"))
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, org.hamcrest.Matchers.containsString("guide.txt")))
            .andExpect(content().bytes("file-content".getBytes()));

        mockMvc.perform(get("/api/files/{fileId}", fileId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.downloadCount").value(1))
            .andExpect(jsonPath("$.downloadHistories[0].downloadedBy").value("audit-user"))
            .andExpect(jsonPath("$.downloadHistories[0].reason").value("감사 검토"));

        mockMvc.perform(get("/api/files/{fileId}/download-histories", fileId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].clientIp").exists())
            .andExpect(jsonPath("$[0].userAgent").value("JUnit"));

        mockMvc.perform(delete("/api/files/{fileId}", fileId))
            .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/files/{fileId}", fileId))
            .andExpect(status().isNotFound());
    }

    private void cleanStorage() throws IOException {
        Path storage = Path.of("build/test-storage");
        if (Files.exists(storage)) {
            try (var paths = Files.walk(storage)) {
                paths.sorted((left, right) -> right.compareTo(left))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            }
        }
        Files.createDirectories(storage);
        assertThat(Files.exists(storage)).isTrue();
    }
}
