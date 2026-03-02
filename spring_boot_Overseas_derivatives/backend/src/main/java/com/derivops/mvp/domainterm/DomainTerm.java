package com.derivops.mvp.domainterm;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.envers.Audited;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Audited
@Table(name = "domain_terms")
public class DomainTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String domainKey;

    @Column(nullable = false, length = 100)
    private String domainName;

    @Column(nullable = false, unique = true, length = 80)
    private String termKey;

    @Column(nullable = false, length = 120)
    private String termName;

    @Column(nullable = false, length = 120)
    private String koreanName;

    @Column(nullable = false, length = 700)
    private String description;

    @Column(length = 700)
    private String exampleText;

    @Column(nullable = false)
    private int domainSortOrder;

    @Column(nullable = false)
    private int sortOrder;

    @Column(nullable = false)
    private boolean enabled;

    @CreationTimestamp
    @Column(nullable = false)
    private OffsetDateTime createdAt;
}
