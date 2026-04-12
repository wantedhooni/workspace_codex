package com.example.samplefilemanage.service;

import com.example.samplefilemanage.dto.FileDetailResponse;
import com.example.samplefilemanage.dto.FileDownloadCommand;
import com.example.samplefilemanage.dto.FileDownloadHistoryResponse;
import com.example.samplefilemanage.dto.FileSummaryResponse;
import com.example.samplefilemanage.dto.FileUploadResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 메타데이터와 다운로드 이력을 통합 관리하는 서비스 계약이다.
 */
public interface FileManagementService {

    /**
     * 업로드된 파일을 로컬 저장소에 저장하고 메타데이터를 생성한다.
     */
    FileUploadResponse upload(MultipartFile file, String uploadedBy, String description);

    /**
     * 활성 상태 파일 목록을 최신 업로드 순으로 조회한다.
     */
    List<FileSummaryResponse> getFiles();

    /**
     * 단일 파일의 메타데이터와 다운로드 이력을 함께 조회한다.
     */
    FileDetailResponse getFileDetail(Long fileId);

    /**
     * 파일을 읽어 다운로드 리소스를 반환하고 다운로드 이력을 적재한다.
     */
    FileDownloadResource download(Long fileId, FileDownloadCommand command);

    /**
     * 특정 파일의 다운로드 이력만 별도로 조회한다.
     */
    List<FileDownloadHistoryResponse> getDownloadHistories(Long fileId);

    /**
     * 파일을 삭제 상태로 변경하고 로컬 저장 파일을 제거한다.
     */
    void delete(Long fileId);
}
