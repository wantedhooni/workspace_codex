package com.portal.admin.menu.domain;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "menu_items")
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String path;

    @Column(name = "parent_id")
    private Long parentId;

    private Integer sortOrder;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", referencedColumnName = "id", insertable = false, updatable = false)
    @OrderBy("sortOrder asc, id asc")
    private List<MenuItem> children = new ArrayList<>();

    protected MenuItem() {}

    public MenuItem(String title, String path, Long parentId, Integer sortOrder) {
        this.title = title;
        this.path = path;
        this.parentId = parentId;
        this.sortOrder = sortOrder;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public String getPath() { return path; }
    public Long getParentId() { return parentId; }
    public Integer getSortOrder() { return sortOrder; }
    public List<MenuItem> getChildren() { return children; }

    public void setTitle(String title) { this.title = title; }
    public void setPath(String path) { this.path = path; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public void setChildren(List<MenuItem> children) { this.children = children == null ? new ArrayList<>() : new ArrayList<>(children); }
}
