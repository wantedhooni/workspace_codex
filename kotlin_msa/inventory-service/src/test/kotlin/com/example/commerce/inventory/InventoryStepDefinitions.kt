package com.example.commerce.inventory

import io.cucumber.java.Before
import io.cucumber.java.ko.그리고
import io.cucumber.java.ko.그러면
import io.cucumber.java.ko.만일
import io.cucumber.java.ko.조건
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy

class InventoryStepDefinitions {
    private lateinit var inventoryService: InventoryService
    private var response: ReserveInventoryResponse? = null
    private var targetSku: String = ""

    @Before
    fun setUp() {
        inventoryService = InventoryService()
        response = null
        targetSku = ""
    }

    @조건("SKU {string} 의 가용 재고가 {int}개 있다")
    fun skuHasAvailableInventory(sku: String, quantity: Int) {
        targetSku = sku
        assertThat(inventoryService.findBySku(sku).availableQuantity).isEqualTo(quantity)
    }

    @만일("SKU {string} 재고 {int}개를 예약한다")
    fun reserveInventory(sku: String, quantity: Int) {
        response = inventoryService.reserve(ReserveInventoryRequest("ORD-001", sku, quantity))
    }

    @그러면("재고 예약은 성공한다")
    fun reservationShouldSucceed() {
        assertThat(response?.accepted).isTrue()
    }

    @그러면("재고 예약은 거절된다")
    fun reservationShouldBeRejected() {
        assertThat(response?.accepted).isFalse()
    }

    @그리고("SKU {string} 의 가용 재고는 {int}개이고 예약 재고는 {int}개이다")
    fun inventoryShouldBe(sku: String, available: Int, reserved: Int) {
        val inventory = inventoryService.findBySku(sku)
        assertThat(inventory.availableQuantity).isEqualTo(available)
        assertThat(inventory.reservedQuantity).isEqualTo(reserved)
    }

    @조건("SKU {string} 재고 {int}개가 예약되어 있다")
    fun inventoryIsReserved(sku: String, quantity: Int) {
        targetSku = sku
        response = inventoryService.reserve(ReserveInventoryRequest("ORD-002", sku, quantity))
        assertThat(response?.accepted).isTrue()
    }

    @만일("SKU {string} 예약 재고 {int}개를 해제한다")
    fun releaseInventory(sku: String, quantity: Int) {
        inventoryService.release(ReleaseInventoryRequest("ORD-002", sku, quantity))
    }

    @조건("존재하지 않는 SKU {string} 이 있다")
    fun unknownSkuExists(sku: String) {
        targetSku = sku
    }

    @만일("해당 SKU의 재고를 조회한다")
    fun findUnknownInventory() {
    }

    @그러면("재고 없음 예외가 발생한다")
    fun inventoryNotFoundExceptionShouldBeThrown() {
        assertThatThrownBy { inventoryService.findBySku(targetSku) }
            .isInstanceOf(InventoryNotFoundException::class.java)
            .hasMessageContaining(targetSku)
    }
}

