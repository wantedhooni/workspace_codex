package com.portal.admin.repo.search;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface SearchableRepository<E> {

    List<E> search(String query);

    default Page<E> search(String query, Pageable pageable) {
        List<E> all = search(query);
        if (pageable == null || pageable.isUnpaged()) {
            return new PageImpl<>(all);
        }
        int fromIndex = (int) Math.min(pageable.getOffset(), all.size());
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), all.size());
        return new PageImpl<>(all.subList(fromIndex, toIndex), pageable, all.size());
    }

    Set<String> allowedSortFields();
}
