package com.example.commerce.product

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import java.math.BigDecimal

/**
 * 판매 가능한 상품의 현재 상태를 표현합니다.
 */
data class Product(
    val id: String,
    val sku: String,
    val name: String,
    val description: String,
    val price: BigDecimal,
    val status: ProductStatus,
)

/**
 * 상품 판매 상태를 정의합니다.
 */
enum class ProductStatus {
    ACTIVE,
    INACTIVE,
}

/**
 * 상품 등록 요청 본문입니다.
 */
data class CreateProductRequest(
    @field:NotBlank val sku: String,
    @field:NotBlank val name: String,
    val description: String = "",
    @field:DecimalMin("0.0", inclusive = false) val price: BigDecimal,
)

/**
 * 상품 상태 변경 요청 본문입니다.
 */
data class UpdateProductStatusRequest(
    val status: ProductStatus,
)

