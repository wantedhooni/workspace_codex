package com.example.samplefilemanage.web;

import com.example.samplefilemanage.dto.FileDetailResponse;
import com.example.samplefilemanage.dto.FileDownloadCommand;
import com.example.samplefilemanage.dto.FileDownloadHistoryResponse;
import com.example.samplefilemanage.dto.FileSummaryResponse;
import com.example.samplefilemanage.dto.FileUploadResponse;
import com.example.samplefilemanage.service.FileDownloadResource;
import com.example.samplefilemanage.service.FileManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Size;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 업로드, 다운로드, 이력 조회 API를 제공하는 컨트롤러이다.
 */
@Validated
@RestController
@RequestMapping("/api/files")
public class FileManagementController {

    private final FileManagementService fileManagementService;

    public FileManagementController(FileManagementService fileManagementService) {
        this.fileManagementService = fileManagementService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileUploadResponse upload(
        @RequestParam("file") MultipartFile file,
        @RequestParam("uploadedBy") @Size(max = 100) String uploadedBy,
        @RequestParam(value = "description", required = false) @Size(max = 500) String description
    ) {
        return fileManagementService.upload(file, uploadedBy, description);
    }

    @GetMapping
    public List<FileSummaryResponse> getFiles() {
        return fileManagementService.getFiles();
    }

    @GetMapping("/{fileId}")
    public FileDetailResponse getFileDetail(@PathVariable Long fileId) {
        return fileManagementService.getFileDetail(fileId);
    }

    @GetMapping("/{fileId}/download")
    public ResponseEntity<org.springframework.core.io.Resource> download(
        @PathVariable Long fileId,
        @RequestParam(value = "downloadedBy", required = false) @Size(max = 100) String downloadedBy,
        @RequestParam(value = "reason", required = false) @Size(max = 500) String reason,
        @RequestHeader(value = HttpHeaders.USER_AGENT, required = false) String userAgent,
        HttpServletRequest request
    ) {
        FileDownloadResource resource = fileManagementService.download(
            fileId,
            new FileDownloadCommand(downloadedBy, reason, extractClientIp(request), userAgent)
        );

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(resource.storedFile().getContentType()))
            .contentLength(resource.storedFile().getFileSize())
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename(resource.storedFile().getOriginalFileName(), StandardCharsets.UTF_8)
                .build()
                .toString())
            .body(resource.resource());
    }

    @GetMapping("/{fileId}/download-histories")
    public List<FileDownloadHistoryResponse> getDownloadHistories(@PathVariable Long fileId) {
        return fileManagementService.getDownloadHistories(fileId);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> delete(@PathVariable Long fileId) {
        fileManagementService.delete(fileId);
        return ResponseEntity.noContent().build();
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
