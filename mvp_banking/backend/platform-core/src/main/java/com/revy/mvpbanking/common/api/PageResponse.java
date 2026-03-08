package com.revy.mvpbanking.common.api;

import java.util.List;
import org.springframework.data.domain.Page;

public record PageResponse<T>(
        List<T> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }

    public static <T> PageResponse<T> from(List<T> items, int page, int size) {
        int normalizedPage = Math.max(page, 0);
        int normalizedSize = Math.max(size, 1);
        int totalElements = items.size();
        int fromIndex = Math.min(normalizedPage * normalizedSize, totalElements);
        int toIndex = Math.min(fromIndex + normalizedSize, totalElements);
        int totalPages = totalElements == 0 ? 0 : (int) Math.ceil((double) totalElements / normalizedSize);
        return new PageResponse<>(
                items.subList(fromIndex, toIndex),
                normalizedPage,
                normalizedSize,
                totalElements,
                totalPages
        );
    }
}
