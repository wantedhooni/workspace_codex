package com.example.samplefilestream.storage;

import com.example.samplefilestream.exception.StorageBackendUnavailableException;
import com.example.samplefilestream.file.domain.FileStorageType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class StorageRegistry {

    private final Map<FileStorageType, FileContentStorage> storages = new EnumMap<>(FileStorageType.class);

    public StorageRegistry(List<FileContentStorage> storageImplementations) {
        for (FileContentStorage storage : storageImplementations) {
            storages.put(storage.type(), storage);
        }
    }

    public FileContentStorage get(FileStorageType storageType) {
        FileContentStorage storage = storages.get(storageType);
        if (storage == null) {
            throw new StorageBackendUnavailableException(storageType + " 백엔드가 활성화되지 않았습니다.");
        }
        return storage;
    }
}
