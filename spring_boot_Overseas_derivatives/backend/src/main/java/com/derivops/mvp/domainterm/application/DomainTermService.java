package com.derivops.mvp.domainterm.application;

import com.derivops.mvp.domainterm.DomainTerm;
import com.derivops.mvp.domainterm.dto.DomainTermResponse;
import com.derivops.mvp.domainterm.infrastructure.DomainTermRepository;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class DomainTermService {

    private final DomainTermRepository domainTermRepository;

    @Transactional(readOnly = true)
    public List<DomainTermResponse> list(String domainKey) {
        if (domainKey == null || domainKey.isBlank()) {
            return domainTermRepository.findByEnabledTrueOrderByDomainSortOrderAscSortOrderAscIdAsc()
                    .stream()
                    .map(this::toResponse)
                    .toList();
        }

        return domainTermRepository.findByEnabledTrueAndDomainKeyOrderBySortOrderAscIdAsc(domainKey.trim().toUpperCase(Locale.ROOT))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private DomainTermResponse toResponse(DomainTerm item) {
        return new DomainTermResponse(
                item.getId(),
                item.getDomainKey(),
                item.getDomainName(),
                item.getTermKey(),
                item.getTermName(),
                item.getKoreanName(),
                item.getDescription(),
                item.getExampleText(),
                item.getDomainSortOrder(),
                item.getSortOrder()
        );
    }
}
