package com.example.commerce.inventory

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

/**
 * SKU별 재고 상태를 표현합니다.
 */
data class Inventory(
    val sku: String,
    val availableQuantity: Int,
    val reservedQuantity: Int,
)

/**
 * 재고 예약 요청 본문입니다.
 */
data class ReserveInventoryRequest(
    @field:NotBlank val orderId: String,
    @field:NotBlank val sku: String,
    @field:Min(1) val quantity: Int,
)

/**
 * 재고 예약 결과를 표현합니다.
 */
data class ReserveInventoryResponse(
    val orderId: String,
    val sku: String,
    val reservedQuantity: Int,
    val accepted: Boolean,
    val reason: String? = null,
)

/**
 * 재고 예약 해제 요청 본문입니다.
 */
data class ReleaseInventoryRequest(
    @field:NotBlank val orderId: String,
    @field:NotBlank val sku: String,
    @field:Min(1) val quantity: Int,
)

