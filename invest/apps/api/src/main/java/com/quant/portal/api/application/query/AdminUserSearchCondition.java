package com.quant.portal.api.application.query;

public record AdminUserSearchCondition(
        String keyword,
        String roleCode
) {
}

