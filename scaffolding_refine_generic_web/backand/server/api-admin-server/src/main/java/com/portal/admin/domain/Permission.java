package com.portal.admin.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "permissions")
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private String description;

    protected Permission() {}

    public Permission(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getDescription() { return description; }

    public void setCode(String code) { this.code = code; }
    public void setDescription(String description) { this.description = description; }
}
