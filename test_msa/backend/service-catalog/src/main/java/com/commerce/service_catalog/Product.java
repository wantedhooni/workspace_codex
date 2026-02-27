package com.commerce.service_catalog;

import jakarta.persistence.*;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String sku;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Integer price;

    protected Product() {}

    public Product(String sku, String name, Integer price) {
        this.sku = sku;
        this.name = name;
        this.price = price;
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public Integer getPrice() { return price; }

    public void setName(String name) { this.name = name; }
    public void setPrice(Integer price) { this.price = price; }
}
