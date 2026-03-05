package com.example.samplefilestream.storage;

import com.example.samplefilestream.file.domain.FileStorageType;
import java.nio.file.Path;

public interface FileContentStorage {

    FileStorageType type();

    void upload(Path stagedFile, String objectKey, String contentType);

    DownloadedContent download(String objectKey);
}
