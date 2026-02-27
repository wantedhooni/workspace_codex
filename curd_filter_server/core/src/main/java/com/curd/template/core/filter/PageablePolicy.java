package com.curd.template.core.filter;

import com.curd.template.core.error.ApiException;
import com.curd.template.core.error.AppErrorCode;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class PageablePolicy {

    public Pageable build(int page, int size, int maxPageSize, Sort sort) {
        if (page < 0) {
            throw new ApiException(AppErrorCode.INVALID_PAGINATION, "page must be >= 0");
        }
        if (size <= 0) {
            throw new ApiException(AppErrorCode.INVALID_PAGINATION, "size must be > 0");
        }
        if (size > maxPageSize) {
            throw new ApiException(
                AppErrorCode.INVALID_PAGINATION,
                "size exceeds max page size: " + maxPageSize
            );
        }

        return PageRequest.of(page, size, sort);
    }
}
