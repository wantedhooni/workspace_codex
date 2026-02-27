package com.portal.admin.repo;

import com.portal.admin.domain.ProgramItem;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProgramItemRepository extends JpaRepository<ProgramItem, Long>, SearchableRepository<ProgramItem>, FieldSearchableRepository<ProgramItem> {
    Optional<ProgramItem> findFirstByUrlAndHttpMethod(String url, String httpMethod);
}
