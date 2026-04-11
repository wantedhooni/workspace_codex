package com.example.samplerecommend.web;

import com.example.samplerecommend.dto.ProductResponse;
import com.example.samplerecommend.service.CatalogService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 추천 대상 카탈로그 조회 API를 제공한다.
 */
@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    private final CatalogService catalogService;

    public CatalogController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    /**
     * 전체 상품 목록을 반환한다.
     */
    @GetMapping("/products")
    public List<ProductResponse> getProducts() {
        return catalogService.getProducts();
    }
}

