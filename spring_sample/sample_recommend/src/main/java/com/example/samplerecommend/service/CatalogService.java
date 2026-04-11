package com.example.samplerecommend.service;

import com.example.samplerecommend.dto.ProductResponse;
import com.example.samplerecommend.repository.ProductRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 추천 대상 카탈로그 조회를 담당한다.
 */
@Service
@Transactional(readOnly = true)
public class CatalogService {

    private final ProductRepository productRepository;

    public CatalogService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * 전체 상품 목록을 응답 DTO로 변환한다.
     */
    public List<ProductResponse> getProducts() {
        return productRepository.findAll().stream()
                .map(product -> new ProductResponse(
                        product.getProductCode(),
                        product.getName(),
                        product.getCategory(),
                        product.getPrice(),
                        product.getStockQuantity(),
                        product.getPopularityScore()
                ))
                .toList();
    }
}
