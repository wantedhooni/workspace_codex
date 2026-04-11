package com.example.samplerecommend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * 고객의 추천 기준이 되는 프로필 정보를 보관한다.
 */
@Entity
@Table(name = "customer_profiles")
public class CustomerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String customerId;

    @Column(nullable = false, length = 40)
    private String preferredCategory;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal maxPreferredPrice;

    @Column(nullable = false)
    private String membershipLevel;

    protected CustomerProfile() {
    }

    public CustomerProfile(String customerId, String preferredCategory, BigDecimal maxPreferredPrice,
                           String membershipLevel) {
        this.customerId = customerId;
        this.preferredCategory = preferredCategory;
        this.maxPreferredPrice = maxPreferredPrice;
        this.membershipLevel = membershipLevel;
    }

    public Long getId() {
        return id;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getPreferredCategory() {
        return preferredCategory;
    }

    public BigDecimal getMaxPreferredPrice() {
        return maxPreferredPrice;
    }

    public String getMembershipLevel() {
        return membershipLevel;
    }
}

