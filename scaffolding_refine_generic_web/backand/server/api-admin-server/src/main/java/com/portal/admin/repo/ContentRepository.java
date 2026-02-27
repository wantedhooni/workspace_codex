package com.portal.admin.repo;

import com.portal.admin.domain.ContentPage;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContentRepository extends JpaRepository<ContentPage, Long>, SearchableRepository<ContentPage>, FieldSearchableRepository<ContentPage> {
    boolean existsBySlug(String slug);
}
