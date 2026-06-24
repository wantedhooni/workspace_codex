package com.example.commerce.product

import io.cucumber.java.Before
import io.cucumber.java.ko.그리고
import io.cucumber.java.ko.그러면
import io.cucumber.java.ko.만일
import io.cucumber.java.ko.조건
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import java.math.BigDecimal

class ProductStepDefinitions {
    private lateinit var productService: ProductService
    private lateinit var createRequest: CreateProductRequest
    private var createdProduct: Product? = null
    private var targetSku: String = ""
    private var targetStatus: ProductStatus = ProductStatus.ACTIVE

    @Before
    fun setUp() {
        productService = ProductService()
        createdProduct = null
        targetSku = ""
        targetStatus = ProductStatus.ACTIVE
    }

    @조건("상품 서비스가 초기 상품을 가지고 있다")
    fun initialProductsExist() {
        assertThat(productService.findAll()).hasSize(3)
    }

    @만일("전체 상품 목록을 조회한다")
    fun findAllProducts() {
        createdProduct = null
    }

    @그러면("상품 SKU 목록은 {string} 이다")
    fun productSkuListShouldBe(expectedCsv: String) {
        assertThat(productService.findAll().map { it.sku }).containsExactlyElementsOf(expectedCsv.split(","))
    }

    @조건("상품 등록 요청이 준비되어 있다")
    fun productCreateRequestIsPrepared() {
        createRequest = CreateProductRequest(
            sku = "SKU-100",
            name = "테스트 상품",
            description = "BDD 테스트 상품",
            price = BigDecimal("25000"),
        )
    }

    @만일("상품을 등록한다")
    fun createProduct() {
        createdProduct = productService.create(createRequest)
    }

    @그러면("상품은 {string} 상태로 저장된다")
    fun productShouldBeSavedAs(status: String) {
        assertThat(createdProduct?.status).isEqualTo(ProductStatus.valueOf(status))
    }

    @그리고("등록한 SKU로 상품을 다시 조회할 수 있다")
    fun createdProductCanBeFoundBySku() {
        assertThat(productService.findBySku(createdProduct!!.sku)).isEqualTo(createdProduct)
    }

    @조건("이미 등록된 SKU {string} 이 있다")
    fun alreadyRegisteredSkuExists(sku: String) {
        targetSku = sku
        assertThat(productService.findBySku(sku)).isNotNull()
    }

    @만일("같은 SKU로 상품을 등록한다")
    fun createProductWithSameSku() {
        createRequest = CreateProductRequest(
            sku = targetSku,
            name = "중복 상품",
            price = BigDecimal("10000"),
        )
    }

    @그러면("상품 중복 예외가 발생한다")
    fun duplicateProductExceptionShouldBeThrown() {
        assertThatThrownBy { productService.create(createRequest) }
            .isInstanceOf(ProductAlreadyExistsException::class.java)
            .hasMessageContaining(targetSku)
    }

    @조건("SKU {string} 상품이 등록되어 있다")
    fun productExists(sku: String) {
        targetSku = sku
        productService.findBySku(sku)
    }

    @만일("상품 상태를 {string} 로 변경한다")
    fun updateProductStatus(status: String) {
        targetStatus = ProductStatus.valueOf(status)
        productService.updateStatus(targetSku, UpdateProductStatusRequest(targetStatus))
    }

    @그러면("SKU {string} 상품 상태는 {string} 이다")
    fun productStatusShouldBe(sku: String, status: String) {
        assertThat(productService.findBySku(sku).status).isEqualTo(ProductStatus.valueOf(status))
    }
}

