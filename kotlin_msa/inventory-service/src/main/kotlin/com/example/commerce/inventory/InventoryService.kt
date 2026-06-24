package com.example.commerce.inventory

import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

/**
 * SKU별 재고 조회와 예약 처리를 담당하는 서비스입니다.
 *
 * 주문 서비스가 재고를 선점할 수 있도록 원자적 예약 메서드를 제공합니다.
 */
@Service
class InventoryService {
    private val inventories = ConcurrentHashMap<String, Inventory>()

    init {
        inventories["SKU-001"] = Inventory("SKU-001", availableQuantity = 50, reservedQuantity = 0)
        inventories["SKU-002"] = Inventory("SKU-002", availableQuantity = 120, reservedQuantity = 0)
        inventories["SKU-003"] = Inventory("SKU-003", availableQuantity = 80, reservedQuantity = 0)
    }

    /**
     * 모든 SKU의 재고 현황을 조회합니다.
     */
    fun findAll(): List<Inventory> =
        inventories.values.sortedBy { it.sku }

    /**
     * 특정 SKU의 재고 현황을 조회합니다.
     */
    fun findBySku(sku: String): Inventory =
        inventories[sku] ?: throw InventoryNotFoundException(sku)

    /**
     * 주문 생성을 위해 지정 수량의 재고를 예약합니다.
     */
    @Synchronized
    fun reserve(request: ReserveInventoryRequest): ReserveInventoryResponse {
        val current = findBySku(request.sku)
        if (current.availableQuantity < request.quantity) {
            return ReserveInventoryResponse(
                orderId = request.orderId,
                sku = request.sku,
                reservedQuantity = 0,
                accepted = false,
                reason = "가용 재고가 부족합니다.",
            )
        }

        inventories[request.sku] = current.copy(
            availableQuantity = current.availableQuantity - request.quantity,
            reservedQuantity = current.reservedQuantity + request.quantity,
        )

        return ReserveInventoryResponse(
            orderId = request.orderId,
            sku = request.sku,
            reservedQuantity = request.quantity,
            accepted = true,
        )
    }

    /**
     * 주문 실패 또는 취소 시 예약된 재고를 해제합니다.
     */
    @Synchronized
    fun release(request: ReleaseInventoryRequest): Inventory {
        val current = findBySku(request.sku)
        val releasableQuantity = request.quantity.coerceAtMost(current.reservedQuantity)
        val updated = current.copy(
            availableQuantity = current.availableQuantity + releasableQuantity,
            reservedQuantity = current.reservedQuantity - releasableQuantity,
        )
        inventories[request.sku] = updated
        return updated
    }
}

/**
 * 재고 정보를 찾을 수 없을 때 발생하는 예외입니다.
 */
class InventoryNotFoundException(sku: String) : RuntimeException("재고 정보를 찾을 수 없습니다. sku=$sku")

