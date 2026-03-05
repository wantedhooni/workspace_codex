package com.example.samplefilestream.service;

import com.example.samplefilestream.config.StorageProperties;
import com.example.samplefilestream.exception.FileMetadataNotFoundException;
import com.example.samplefilestream.exception.FileStorageOperationException;
import com.example.samplefilestream.file.domain.FileMetadata;
import com.example.samplefilestream.file.domain.FileMetadataRepository;
import com.example.samplefilestream.file.domain.FileStorageType;
import com.example.samplefilestream.storage.FileContentStorage;
import com.example.samplefilestream.storage.StorageRegistry;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final StorageRegistry storageRegistry;
    private final StorageProperties storageProperties;

    public FileService(
        FileMetadataRepository fileMetadataRepository,
        StorageRegistry storageRegistry,
        StorageProperties storageProperties
    ) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.storageRegistry = storageRegistry;
        this.storageProperties = storageProperties;
    }

    @Transactional
    public FileMetadata upload(MultipartFile file, FileStorageType preferredStorageType) {
        if (file.isEmpty()) {
            throw new FileStorageOperationException("빈 파일은 업로드할 수 없습니다.");
        }

        FileStorageType storageType = preferredStorageType != null
            ? preferredStorageType
            : storageProperties.getDefaultType();

        FileContentStorage storage = storageRegistry.get(storageType);

        Path tempFile = createTempFile();
        try {
            FileDigestResult digestResult = copyAndDigest(file, tempFile);
            String objectKey = FileKeyGenerator.generate(file.getOriginalFilename());
            storage.upload(tempFile, objectKey, file.getContentType());

            FileMetadata metadata = new FileMetadata(
                fallbackFilename(file.getOriginalFilename()),
                extractStoredFilename(objectKey),
                storageType,
                objectKey,
                digestResult.sizeBytes(),
                file.getContentType(),
                digestResult.sha256()
            );
            return fileMetadataRepository.save(metadata);
        } finally {
            deleteTempFile(tempFile);
        }
    }

    @Transactional(readOnly = true)
    public FileMetadata getMetadata(UUID id) {
        return fileMetadataRepository.findById(id)
            .orElseThrow(() -> new FileMetadataNotFoundException("파일 메타데이터를 찾을 수 없습니다. id=" + id));
    }

    @Transactional(readOnly = true)
    public List<FileMetadata> list() {
        return fileMetadataRepository.findTop100ByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public FileDownloadView download(UUID id) {
        FileMetadata metadata = getMetadata(id);
        FileContentStorage storage = storageRegistry.get(metadata.getStorageType());
        return new FileDownloadView(metadata, storage.download(metadata.getStoragePath()));
    }

    private Path createTempFile() {
        try {
            return Files.createTempFile("upload-staged-", ".tmp");
        } catch (IOException e) {
            throw new FileStorageOperationException("임시 파일 생성에 실패했습니다.", e);
        }
    }

    private FileDigestResult copyAndDigest(MultipartFile multipartFile, Path tempFile) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            long size;
            try (
                InputStream inputStream = multipartFile.getInputStream();
                DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest);
                var outputStream = Files.newOutputStream(tempFile)
            ) {
                size = digestInputStream.transferTo(outputStream);
            }

            String hash = HexFormat.of().formatHex(digest.digest());
            return new FileDigestResult(size, hash);
        } catch (IOException e) {
            throw new FileStorageOperationException("업로드 파일 처리 중 오류가 발생했습니다.", e);
        } catch (NoSuchAlgorithmException e) {
            throw new FileStorageOperationException("SHA-256 알고리즘을 사용할 수 없습니다.", e);
        }
    }

    private void deleteTempFile(Path tempFile) {
        try {
            Files.deleteIfExists(tempFile);
        } catch (IOException ignored) {
            // 임시 파일 정리 실패는 업로드/다운로드 결과에 영향이 없으므로 로그 없이 무시한다.
        }
    }

    private String fallbackFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return "unnamed.bin";
        }
        return originalFilename;
    }

    private String extractStoredFilename(String objectKey) {
        int index = objectKey.lastIndexOf('/');
        if (index < 0 || index == objectKey.length() - 1) {
            return objectKey;
        }
        return objectKey.substring(index + 1);
    }

    private record FileDigestResult(long sizeBytes, String sha256) {
    }
}
