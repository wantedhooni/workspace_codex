package com.example.samplefilemanage.repository;

import com.example.samplefilemanage.entity.FileStatus;
import com.example.samplefilemanage.entity.StoredFile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 파일 메타데이터를 조회하고 저장하는 저장소이다.
 */
public interface StoredFileRepository extends JpaRepository<StoredFile, Long> {

    List<StoredFile> findAllByStatusOrderByUploadedAtDesc(FileStatus status);

    Optional<StoredFile> findByIdAndStatus(Long id, FileStatus status);
}
