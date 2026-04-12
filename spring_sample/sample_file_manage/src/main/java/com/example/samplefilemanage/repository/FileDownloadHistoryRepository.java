package com.example.samplefilemanage.repository;

import com.example.samplefilemanage.entity.FileDownloadHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 파일 다운로드 이력을 조회하고 저장하는 저장소이다.
 */
public interface FileDownloadHistoryRepository extends JpaRepository<FileDownloadHistory, Long> {

    List<FileDownloadHistory> findByStoredFileIdOrderByDownloadedAtDesc(Long storedFileId);
}
