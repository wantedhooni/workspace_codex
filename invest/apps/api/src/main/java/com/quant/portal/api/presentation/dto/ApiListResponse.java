package com.quant.portal.api.presentation.dto;

import java.util.List;

public record ApiListResponse<T>(
        List<T> data,
        long total
) {
}
