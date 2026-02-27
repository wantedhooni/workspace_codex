package com.portal.admin.domain;

import jakarta.persistence.*;

@Entity
@Table(
        name = "program_items",
        uniqueConstraints = @UniqueConstraint(columnNames = {"url", "http_method"})
)
public class ProgramItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String url;

    @Column(name = "http_method", nullable = false)
    private String httpMethod;

    private String description;

    @Column(nullable = false)
    private Boolean enabled;

    protected ProgramItem() {}

    public ProgramItem(String name, String url, String httpMethod, String description, Boolean enabled) {
        this.name = name;
        this.url = url;
        this.httpMethod = httpMethod;
        this.description = description;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getUrl() { return url; }
    public String getHttpMethod() { return httpMethod; }
    public String getDescription() { return description; }
    public Boolean getEnabled() { return enabled; }

    public void setName(String name) { this.name = name; }
    public void setUrl(String url) { this.url = url; }
    public void setHttpMethod(String httpMethod) { this.httpMethod = httpMethod; }
    public void setDescription(String description) { this.description = description; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
