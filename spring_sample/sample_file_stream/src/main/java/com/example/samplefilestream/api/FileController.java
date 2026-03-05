package com.example.samplefilestream.api;

import com.example.samplefilestream.file.domain.FileMetadata;
import com.example.samplefilestream.file.domain.FileStorageType;
import com.example.samplefilestream.service.FileDownloadView;
import com.example.samplefilestream.service.FileService;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileMetadataResponse> upload(
        @RequestPart("file") MultipartFile file,
        @RequestParam(required = false) FileStorageType storageType
    ) {
        FileMetadata saved = fileService.upload(file, storageType);
        return ResponseEntity.created(
                ServletUriComponentsBuilder.fromCurrentRequest()
                    .path("/{id}")
                    .buildAndExpand(saved.getId())
                    .toUri()
            )
            .body(FileMetadataResponse.from(saved));
    }

    @GetMapping("/{id}")
    public FileMetadataResponse getMetadata(@PathVariable UUID id) {
        return FileMetadataResponse.from(fileService.getMetadata(id));
    }

    @GetMapping
    public List<FileMetadataResponse> list() {
        return fileService.list().stream()
            .map(FileMetadataResponse::from)
            .toList();
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<?> download(@PathVariable UUID id) {
        FileDownloadView fileDownloadView = fileService.download(id);
        FileMetadata metadata = fileDownloadView.metadata();
        String contentType = resolveContentType(fileDownloadView);

        ContentDisposition disposition = ContentDisposition.attachment()
            .filename(metadata.getOriginalFilename(), StandardCharsets.UTF_8)
            .build();

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .contentLength(fileDownloadView.downloadedContent().contentLength())
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .body(fileDownloadView.downloadedContent().resource());
    }

    private String resolveContentType(FileDownloadView fileDownloadView) {
        String contentType = fileDownloadView.downloadedContent().contentType();
        if (contentType != null && !contentType.isBlank()) {
            return contentType;
        }

        String metadataContentType = fileDownloadView.metadata().getContentType();
        if (metadataContentType != null && !metadataContentType.isBlank()) {
            return metadataContentType;
        }

        return MediaType.APPLICATION_OCTET_STREAM_VALUE;
    }
}
