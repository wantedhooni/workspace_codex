package com.example.samplerecommend.service;

import com.example.samplerecommend.domain.CustomerAction;
import com.example.samplerecommend.domain.CustomerProfile;
import com.example.samplerecommend.domain.Product;
import com.example.samplerecommend.domain.RecommendationRequestLog;
import com.example.samplerecommend.dto.RecommendationItemResponse;
import com.example.samplerecommend.dto.RecommendationResponse;
import com.example.samplerecommend.repository.CustomerActionRepository;
import com.example.samplerecommend.repository.CustomerProfileRepository;
import com.example.samplerecommend.repository.ProductRepository;
import com.example.samplerecommend.repository.RecommendationRequestLogRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 고객 프로필과 행동 이력을 기반으로 추천 결과를 계산한다.
 */
@Service
@Transactional(readOnly = true)
public class RecommendationService {

    private final CustomerProfileRepository customerProfileRepository;
    private final CustomerActionRepository customerActionRepository;
    private final ProductRepository productRepository;
    private final RecommendationRequestLogRepository recommendationRequestLogRepository;

    public RecommendationService(CustomerProfileRepository customerProfileRepository,
                                 CustomerActionRepository customerActionRepository,
                                 ProductRepository productRepository,
                                 RecommendationRequestLogRepository recommendationRequestLogRepository) {
        this.customerProfileRepository = customerProfileRepository;
        this.customerActionRepository = customerActionRepository;
        this.productRepository = productRepository;
        this.recommendationRequestLogRepository = recommendationRequestLogRepository;
    }

    /**
     * 고객별 추천 결과를 계산하고 요청 로그를 저장한다.
     */
    @Transactional
    public RecommendationResponse recommend(String customerId, int limit) {
        CustomerProfile profile = customerProfileRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new EntityNotFoundException("고객 프로필을 찾을 수 없습니다. customerId=" + customerId));
        List<CustomerAction> recentActions = customerActionRepository.findTop10ByCustomerIdOrderByActedAtDesc(customerId);
        Map<String, Double> actionWeights = recentActions.stream()
                .collect(Collectors.groupingBy(CustomerAction::getProductCode, Collectors.summingDouble(CustomerAction::getWeight)));

        List<RecommendationCandidate> candidates = productRepository.findAll().stream()
                .filter(product -> product.getStockQuantity() > 0)
                .map(product -> scoreProduct(profile, actionWeights, product))
                .sorted(Comparator.comparingDouble(RecommendationCandidate::score).reversed())
                .limit(limit)
                .toList();

        recommendationRequestLogRepository.save(new RecommendationRequestLog(
                customerId,
                limit,
                candidates.stream().map(candidate -> candidate.product().getProductCode()).collect(Collectors.joining(",")),
                LocalDateTime.now()
        ));

        return new RecommendationResponse(
                customerId,
                limit,
                LocalDateTime.now(),
                candidates.stream()
                        .map(candidate -> new RecommendationItemResponse(
                                candidate.product().getProductCode(),
                                candidate.product().getName(),
                                candidate.product().getCategory(),
                                candidate.product().getPrice(),
                                candidate.score(),
                                candidate.reasons()
                        ))
                        .toList()
        );
    }

    private RecommendationCandidate scoreProduct(CustomerProfile profile, Map<String, Double> actionWeights, Product product) {
        double score = product.getPopularityScore() * 0.35;
        List<String> reasons = new ArrayList<>();

        if (product.getCategory().equalsIgnoreCase(profile.getPreferredCategory())) {
            score += 35;
            reasons.add("선호 카테고리 일치");
        }

        if (product.getPrice().compareTo(profile.getMaxPreferredPrice()) <= 0) {
            score += 20;
            reasons.add("선호 가격대 이내");
        } else {
            score -= 5;
            reasons.add("선호 가격대 초과");
        }

        double actionBoost = actionWeights.getOrDefault(product.getProductCode(), 0.0) * 6;
        if (actionBoost > 0) {
            score += actionBoost;
            reasons.add("최근 행동 기반 관심도 반영");
        }

        if ("VIP".equalsIgnoreCase(profile.getMembershipLevel())) {
            score += 4;
            reasons.add("VIP 고객 보정");
        }

        if (product.getStockQuantity() < 5) {
            score -= 10;
            reasons.add("재고 부족 패널티");
        } else {
            reasons.add("즉시 판매 가능 재고");
        }

        return new RecommendationCandidate(product, Math.round(score * 100) / 100.0, reasons);
    }
}

