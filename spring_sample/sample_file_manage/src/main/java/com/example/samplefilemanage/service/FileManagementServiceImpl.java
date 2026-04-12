package com.example.samplefilemanage.service;

import com.example.samplefilemanage.config.FileStorageProperties;
import com.example.samplefilemanage.dto.FileDetailResponse;
import com.example.samplefilemanage.dto.FileDownloadCommand;
import com.example.samplefilemanage.dto.FileDownloadHistoryResponse;
import com.example.samplefilemanage.dto.FileSummaryResponse;
import com.example.samplefilemanage.dto.FileUploadResponse;
import com.example.samplefilemanage.entity.FileDownloadHistory;
import com.example.samplefilemanage.entity.FileStatus;
import com.example.samplefilemanage.entity.StoredFile;
import com.example.samplefilemanage.repository.FileDownloadHistoryRepository;
import com.example.samplefilemanage.repository.StoredFileRepository;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * 로컬 파일 시스템과 JPA 테이블을 함께 사용해 파일 관리 기능을 구현한다.
 */
@Service
@Transactional
public class FileManagementServiceImpl implements FileManagementService {

    private static final String DEFAULT_CONTENT_TYPE = MediaType.APPLICATION_OCTET_STREAM_VALUE;

    private final StoredFileRepository storedFileRepository;
    private final FileDownloadHistoryRepository fileDownloadHistoryRepository;
    private final Path storageRoot;

    public FileManagementServiceImpl(
        StoredFileRepository storedFileRepository,
        FileDownloadHistoryRepository fileDownloadHistoryRepository,
        FileStorageProperties properties
    ) {
        this.storedFileRepository = storedFileRepository;
        this.fileDownloadHistoryRepository = fileDownloadHistoryRepository;
        this.storageRoot = Path.of(properties.storagePath()).toAbsolutePath().normalize();
    }

    /**
     * 업로드된 파일을 로컬 저장소에 저장하고 메타데이터를 생성한다.
     */
    @Override
    public FileUploadResponse upload(MultipartFile file, String uploadedBy, String description) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "빈 파일은 업로드할 수 없습니다.");
        }

        String originalFileName = sanitizeFileName(file.getOriginalFilename());
        String extension = extractExtension(originalFileName);
        String storedFileName = UUID.randomUUID() + extension;
        Path destination = storageRoot.resolve(storedFileName).normalize();

        try {
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "파일 저장 중 오류가 발생했습니다.", e);
        }

        StoredFile storedFile = new StoredFile(
            originalFileName,
            storedFileName,
            destination.toString(),
            resolveContentType(file.getContentType()),
            file.getSize(),
            StringUtils.hasText(uploadedBy) ? uploadedBy : "anonymous",
            description
        );

        StoredFile saved = storedFileRepository.save(storedFile);
        return new FileUploadResponse(
            saved.getId(),
            saved.getOriginalFileName(),
            saved.getContentType(),
            saved.getFileSize(),
            saved.getUploadedBy(),
            saved.getStatus().name(),
            saved.getUploadedAt()
        );
    }

    /**
     * 활성 상태 파일 목록을 최신 업로드 순으로 조회한다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<FileSummaryResponse> getFiles() {
        return storedFileRepository.findAllByStatusOrderByUploadedAtDesc(FileStatus.ACTIVE).stream()
            .map(this::toSummaryResponse)
            .toList();
    }

    /**
     * 단일 파일의 메타데이터와 다운로드 이력을 함께 조회한다.
     */
    @Override
    @Transactional(readOnly = true)
    public FileDetailResponse getFileDetail(Long fileId) {
        StoredFile storedFile = getActiveStoredFile(fileId);
        List<FileDownloadHistoryResponse> histories = getDownloadHistories(fileId);
        return new FileDetailResponse(
            storedFile.getId(),
            storedFile.getOriginalFileName(),
            storedFile.getStoredFileName(),
            storedFile.getContentType(),
            storedFile.getFileSize(),
            storedFile.getUploadedBy(),
            storedFile.getDescription(),
            storedFile.getStatus().name(),
            storedFile.getDownloadCount(),
            storedFile.getUploadedAt(),
            storedFile.getLastDownloadedAt(),
            histories
        );
    }

    /**
     * 파일을 읽어 다운로드 리소스를 반환하고 다운로드 이력을 적재한다.
     */
    @Override
    public FileDownloadResource download(Long fileId, FileDownloadCommand command) {
        StoredFile storedFile = getActiveStoredFile(fileId);
        Path filePath = Path.of(storedFile.getStoragePath()).normalize();

        if (!Files.exists(filePath)) {
            throw new ResponseStatusException(NOT_FOUND, "실제 저장 파일을 찾을 수 없습니다.");
        }

        Resource resource = toResource(filePath);
        storedFile.markDownloaded();

        FileDownloadHistory history = new FileDownloadHistory(
            storedFile,
            StringUtils.hasText(command.downloadedBy()) ? command.downloadedBy() : "anonymous",
            command.reason(),
            StringUtils.hasText(command.clientIp()) ? command.clientIp() : "unknown",
            command.userAgent()
        );
        fileDownloadHistoryRepository.save(history);

        return new FileDownloadResource(storedFile, resource);
    }

    /**
     * 특정 파일의 다운로드 이력만 별도로 조회한다.
     */
    @Override
    @Transactional(readOnly = true)
    public List<FileDownloadHistoryResponse> getDownloadHistories(Long fileId) {
        getActiveStoredFile(fileId);
        return fileDownloadHistoryRepository.findByStoredFileIdOrderByDownloadedAtDesc(fileId).stream()
            .map(history -> new FileDownloadHistoryResponse(
                history.getId(),
                history.getDownloadedBy(),
                history.getReason(),
                history.getClientIp(),
                history.getUserAgent(),
                history.getDownloadedAt()
            ))
            .toList();
    }

    /**
     * 파일을 삭제 상태로 변경하고 로컬 저장 파일을 제거한다.
     */
    @Override
    public void delete(Long fileId) {
        StoredFile storedFile = getActiveStoredFile(fileId);
        Path filePath = Path.of(storedFile.getStoragePath()).normalize();
        try {
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "파일 삭제 중 오류가 발생했습니다.", e);
        }
        storedFile.markDeleted();
    }

    private StoredFile getActiveStoredFile(Long fileId) {
        return storedFileRepository.findByIdAndStatus(fileId, FileStatus.ACTIVE)
            .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "파일을 찾을 수 없습니다. fileId=" + fileId));
    }

    private String sanitizeFileName(String originalFilename) {
        String cleaned = StringUtils.cleanPath(originalFilename == null ? "unnamed-file" : originalFilename);
        if (cleaned.contains("..")) {
            throw new ResponseStatusException(BAD_REQUEST, "허용되지 않는 파일명입니다.");
        }
        return cleaned;
    }

    private String extractExtension(String originalFileName) {
        int index = originalFileName.lastIndexOf('.');
        return index >= 0 ? originalFileName.substring(index) : "";
    }

    private String resolveContentType(String contentType) {
        return StringUtils.hasText(contentType) ? contentType : DEFAULT_CONTENT_TYPE;
    }

    private Resource toResource(Path filePath) {
        try {
            return new UrlResource(filePath.toUri());
        } catch (MalformedURLException e) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "다운로드 리소스를 생성할 수 없습니다.", e);
        }
    }

    private FileSummaryResponse toSummaryResponse(StoredFile storedFile) {
        return new FileSummaryResponse(
            storedFile.getId(),
            storedFile.getOriginalFileName(),
            storedFile.getContentType(),
            storedFile.getFileSize(),
            storedFile.getUploadedBy(),
            storedFile.getStatus().name(),
            storedFile.getDownloadCount(),
            storedFile.getUploadedAt(),
            storedFile.getLastDownloadedAt()
        );
    }
}
