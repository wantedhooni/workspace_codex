package com.portal.admin.domain;

import jakarta.persistence.*;

@Entity
@Table(
        name = "common_codes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"group_code", "code"})
)
public class CommonCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_code", nullable = false)
    private String groupCode;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private Integer sortOrder;

    @Column(nullable = false)
    private Boolean enabled;

    protected CommonCode() {}

    public CommonCode(String groupCode, String code, String name, String description, Integer sortOrder, Boolean enabled) {
        this.groupCode = groupCode;
        this.code = code;
        this.name = name;
        this.description = description;
        this.sortOrder = sortOrder;
        this.enabled = enabled;
    }

    public Long getId() { return id; }
    public String getGroupCode() { return groupCode; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Integer getSortOrder() { return sortOrder; }
    public Boolean getEnabled() { return enabled; }

    public void setGroupCode(String groupCode) { this.groupCode = groupCode; }
    public void setCode(String code) { this.code = code; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }
}
