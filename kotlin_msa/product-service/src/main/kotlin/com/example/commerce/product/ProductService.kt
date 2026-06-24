package com.example.commerce.product

import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * 상품 카탈로그 비즈니스 규칙을 처리하는 서비스입니다.
 *
 * 초기 구현은 인메모리 저장소를 사용하며, 실제 운영에서는 JPA/R2DBC 저장소로 교체할 수 있습니다.
 */
@Service
class ProductService {
    private val products = ConcurrentHashMap<String, Product>()

    init {
        seed(Product("PRD-001", "SKU-001", "무선 키보드", "저소음 무선 키보드", BigDecimal("129000"), ProductStatus.ACTIVE))
        seed(Product("PRD-002", "SKU-002", "USB-C 허브", "멀티 포트 USB-C 허브", BigDecimal("59000"), ProductStatus.ACTIVE))
        seed(Product("PRD-003", "SKU-003", "노트북 거치대", "알루미늄 접이식 거치대", BigDecimal("39000"), ProductStatus.ACTIVE))
    }

    /**
     * 판매 중인 모든 상품을 조회합니다.
     */
    fun findAll(): List<Product> =
        products.values.sortedBy { it.sku }

    /**
     * SKU 기준으로 상품을 조회합니다.
     */
    fun findBySku(sku: String): Product =
        products[sku] ?: throw ProductNotFoundException(sku)

    /**
     * 신규 상품을 카탈로그에 등록합니다.
     */
    fun create(request: CreateProductRequest): Product {
        if (products.containsKey(request.sku)) {
            throw ProductAlreadyExistsException(request.sku)
        }

        val product = Product(
            id = "PRD-${UUID.randomUUID().toString().take(8)}",
            sku = request.sku,
            name = request.name,
            description = request.description,
            price = request.price,
            status = ProductStatus.ACTIVE,
        )
        products[product.sku] = product
        return product
    }

    /**
     * 기존 상품의 판매 상태를 변경합니다.
     */
    fun updateStatus(sku: String, request: UpdateProductStatusRequest): Product {
        val current = findBySku(sku)
        val updated = current.copy(status = request.status)
        products[sku] = updated
        return updated
    }

    private fun seed(product: Product) {
        products[product.sku] = product
    }
}

/**
 * 상품을 찾을 수 없을 때 발생하는 예외입니다.
 */
class ProductNotFoundException(sku: String) : RuntimeException("상품을 찾을 수 없습니다. sku=$sku")

/**
 * 동일 SKU 상품이 이미 존재할 때 발생하는 예외입니다.
 */
class ProductAlreadyExistsException(sku: String) : RuntimeException("이미 등록된 상품입니다. sku=$sku")

