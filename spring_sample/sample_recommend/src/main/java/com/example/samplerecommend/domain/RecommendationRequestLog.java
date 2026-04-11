package com.example.samplerecommend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * 추천 요청 이력을 저장해 운영 추적과 검증에 활용한다.
 */
@Entity
@Table(name = "recommendation_request_logs")
public class RecommendationRequestLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String customerId;

    @Column(nullable = false)
    private Integer requestedLimit;

    @Column(nullable = false, length = 400)
    private String recommendedProductCodes;

    @Column(nullable = false)
    private LocalDateTime requestedAt;

    protected RecommendationRequestLog() {
    }

    public RecommendationRequestLog(String customerId, Integer requestedLimit, String recommendedProductCodes,
                                    LocalDateTime requestedAt) {
        this.customerId = customerId;
        this.requestedLimit = requestedLimit;
        this.recommendedProductCodes = recommendedProductCodes;
        this.requestedAt = requestedAt;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public Integer getRequestedLimit() {
        return requestedLimit;
    }

    public String getRecommendedProductCodes() {
        return recommendedProductCodes;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }
}
