package com.example.commerce.product

import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/**
 * 상품 카탈로그 REST API를 제공하는 컨트롤러입니다.
 */
@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService,
) {
    /**
     * 전체 상품 목록을 조회합니다.
     */
    @GetMapping
    fun findAll(): List<Product> =
        productService.findAll()

    /**
     * SKU 기준으로 단일 상품을 조회합니다.
     */
    @GetMapping("/{sku}")
    fun findBySku(@PathVariable sku: String): Product =
        productService.findBySku(sku)

    /**
     * 신규 상품을 등록합니다.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun create(@Valid @RequestBody request: CreateProductRequest): Product =
        productService.create(request)

    /**
     * 상품 판매 상태를 변경합니다.
     */
    @PatchMapping("/{sku}/status")
    fun updateStatus(
        @PathVariable sku: String,
        @RequestBody request: UpdateProductStatusRequest,
    ): Product =
        productService.updateStatus(sku, request)

    /**
     * 상품 도메인 예외를 HTTP 응답으로 변환합니다.
     */
    @ExceptionHandler(ProductNotFoundException::class)
    fun handleNotFound(ex: ProductNotFoundException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.NOT_FOUND).body(mapOf("message" to ex.message.orEmpty()))

    /**
     * 상품 중복 예외를 HTTP 응답으로 변환합니다.
     */
    @ExceptionHandler(ProductAlreadyExistsException::class)
    fun handleConflict(ex: ProductAlreadyExistsException): ResponseEntity<Map<String, String>> =
        ResponseEntity.status(HttpStatus.CONFLICT).body(mapOf("message" to ex.message.orEmpty()))
}

