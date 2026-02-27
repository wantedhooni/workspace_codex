package com.portal.admin.repo;

import com.portal.admin.domain.BannerItem;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BannerItemRepository extends JpaRepository<BannerItem, Long>, SearchableRepository<BannerItem>, FieldSearchableRepository<BannerItem> {
    Optional<BannerItem> findFirstByTitle(String title);
}
