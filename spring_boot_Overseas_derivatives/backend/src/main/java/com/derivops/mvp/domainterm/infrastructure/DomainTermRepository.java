package com.derivops.mvp.domainterm.infrastructure;

import com.derivops.mvp.domainterm.DomainTerm;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DomainTermRepository extends JpaRepository<DomainTerm, Long> {

    List<DomainTerm> findByEnabledTrueOrderByDomainSortOrderAscSortOrderAscIdAsc();

    List<DomainTerm> findByEnabledTrueAndDomainKeyOrderBySortOrderAscIdAsc(String domainKey);

    Optional<DomainTerm> findByTermKey(String termKey);
}
