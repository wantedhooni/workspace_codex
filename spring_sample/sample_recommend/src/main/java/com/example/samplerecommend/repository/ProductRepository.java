package com.example.samplerecommend.repository;

import com.example.samplerecommend.domain.Product;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 상품 카탈로그 조회를 담당한다.
 */
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findByProductCode(String productCode);
}

