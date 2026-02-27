package com.portal.admin.repo;

import com.portal.admin.domain.CommonCode;
import com.portal.admin.repo.search.FieldSearchableRepository;
import com.portal.admin.repo.search.SearchableRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommonCodeRepository extends JpaRepository<CommonCode, Long>, SearchableRepository<CommonCode>, FieldSearchableRepository<CommonCode> {
    Optional<CommonCode> findFirstByGroupCodeAndCode(String groupCode, String code);
}
