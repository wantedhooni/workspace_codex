package com.portal.admin.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "banner_items")
public class BannerItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "link_url")
    private String linkUrl;

    @Column(name = "start_at")
    private String startAt;

    @Column(name = "end_at")
    private String endAt;

    @Column(nullable = false)
    private Boolean enabled;

    @Column(nullable = false)
    private Integer sortOrder;

    protected BannerItem() {}

    public BannerItem(String title, String imageUrl, String linkUrl, String startAt, String endAt, Boolean enabled, Integer sortOrder) {
        this.title = title;
        this.imageUrl = imageUrl;
        this.linkUrl = linkUrl;
        this.startAt = startAt;
        this.endAt = endAt;
        this.enabled = enabled;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getImageUrl() { return imageUrl; }
    public String getLinkUrl() { return linkUrl; }
    public String getStartAt() { return startAt; }
    public String getEndAt() { return endAt; }
    public Boolean getEnabled() { return enabled; }
    public Integer getSortOrder() { return sortOrder; }

    public void setTitle(String title) { this.title = title; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setLinkUrl(String linkUrl) { this.linkUrl = linkUrl; }
    public void setStartAt(String startAt) { this.startAt = startAt; }
    public void setEndAt(String endAt) { this.endAt = endAt; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
}
