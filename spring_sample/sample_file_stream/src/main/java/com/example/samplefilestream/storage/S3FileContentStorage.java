package com.example.samplefilestream.storage;

import com.example.samplefilestream.config.StorageProperties;
import com.example.samplefilestream.exception.FileStorageOperationException;
import com.example.samplefilestream.file.domain.FileStorageType;
import java.nio.file.Path;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Component
@ConditionalOnBean(S3Client.class)
public class S3FileContentStorage implements FileContentStorage {

    private final S3Client s3Client;
    private final String bucket;

    public S3FileContentStorage(S3Client s3Client, StorageProperties storageProperties) {
        this.s3Client = s3Client;
        this.bucket = storageProperties.getS3().getBucket();
    }

    @Override
    public FileStorageType type() {
        return FileStorageType.S3;
    }

    @Override
    public void upload(Path stagedFile, String objectKey, String contentType) {
        try {
            PutObjectRequest.Builder builder = PutObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey);

            if (contentType != null && !contentType.isBlank()) {
                builder.contentType(contentType);
            }

            s3Client.putObject(builder.build(), RequestBody.fromFile(stagedFile));
        } catch (S3Exception e) {
            throw new FileStorageOperationException("S3 파일 업로드에 실패했습니다.", e);
        }
    }

    @Override
    public DownloadedContent download(String objectKey) {
        try {
            GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(objectKey)
                .build();

            ResponseInputStream<GetObjectResponse> stream = s3Client.getObject(request);
            GetObjectResponse response = stream.response();
            return new DownloadedContent(
                new InputStreamResource(stream),
                response.contentLength(),
                response.contentType()
            );
        } catch (NoSuchKeyException e) {
            throw new FileStorageOperationException("S3 파일을 찾을 수 없습니다. key=" + objectKey, e);
        } catch (S3Exception e) {
            throw new FileStorageOperationException("S3 파일 다운로드에 실패했습니다.", e);
        }
    }
}
