package com.commerce.service_catalog;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/catalog")
public class CatalogController {

    private final ProductRepository products;
    private final com.commerce.service_catalog.events.CatalogEventPublisher publisher;

    public CatalogController(ProductRepository products, com.commerce.service_catalog.events.CatalogEventPublisher publisher) {
        this.products = products;
        this.publisher = publisher;
    }

    @GetMapping("/products")
    public List<Product> list() {
        return products.findAll();
    }

    @PostMapping("/products")
    public Product create(@RequestBody ProductRequest request) {
        Product product = new Product(request.sku(), request.name(), request.price());
        Product saved = products.save(product);
        publisher.productUpserted(saved.getSku(), saved.getName(), saved.getPrice());
        return saved;
    }

    @PatchMapping("/products/{id}")
    public Product update(@PathVariable Long id, @RequestBody ProductUpdate request) {
        Product product = products.findById(id).orElseThrow();
        if (request.name() != null) product.setName(request.name());
        if (request.price() != null) product.setPrice(request.price());
        Product saved = products.save(product);
        publisher.productUpserted(saved.getSku(), saved.getName(), saved.getPrice());
        return saved;
    }

    public record ProductRequest(@NotBlank String sku, @NotBlank String name, @NotNull Integer price) {}
    public record ProductUpdate(String name, Integer price) {}
}
