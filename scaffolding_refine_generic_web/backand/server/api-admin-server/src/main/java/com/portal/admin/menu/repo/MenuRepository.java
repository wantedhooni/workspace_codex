package com.portal.admin.menu.repo;

import com.portal.admin.menu.domain.MenuItem;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MenuRepository extends JpaRepository<MenuItem, Long>, SearchableRepository<MenuItem>, FieldSearchableRepository<MenuItem> {
    Optional<MenuItem> findFirstByPath(String path);
    List<MenuItem> findAllByOrderBySortOrderAscIdAsc();
}
