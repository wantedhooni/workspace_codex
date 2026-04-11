package com.example.samplerecommend.repository;

import com.example.samplerecommend.domain.RecommendationRequestLog;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 추천 요청 로그를 조회한다.
 */
public interface RecommendationRequestLogRepository extends JpaRepository<RecommendationRequestLog, Long> {

    List<RecommendationRequestLog> findTop20ByOrderByRequestedAtDesc();
}

