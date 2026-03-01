package com.derivops.mvp.domainterm.dto;

public record DomainTermResponse(
        Long id,
        String domainKey,
        String domainName,
        String termKey,
        String termName,
        String koreanName,
        String description,
        String exampleText,
        int domainSortOrder,
        int sortOrder
) {
}
