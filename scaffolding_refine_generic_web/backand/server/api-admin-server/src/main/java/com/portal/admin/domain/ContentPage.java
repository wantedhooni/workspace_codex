package com.portal.admin.domain;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(name = "content_pages")
public class ContentPage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Lob
    @Column(nullable = false)
    private String body;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Instant updatedAt;

    protected ContentPage() {}

    public ContentPage(String title, String slug, String body, String status) {
        this.title = title;
        this.slug = slug;
        this.body = body;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getSlug() { return slug; }
    public String getBody() { return body; }
    public String getStatus() { return status; }
    public Instant getUpdatedAt() { return updatedAt; }

    public void setTitle(String title) { this.title = title; }
    public void setSlug(String slug) { this.slug = slug; }
    public void setBody(String body) { this.body = body; }
    public void setStatus(String status) { this.status = status; }

    @PrePersist
    @PreUpdate
    void updateTimestamp() {
        this.updatedAt = Instant.now();
    }
}
