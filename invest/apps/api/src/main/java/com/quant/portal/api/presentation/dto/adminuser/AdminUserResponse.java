package com.quant.portal.api.presentation.dto.adminuser;

import java.util.List;

public record AdminUserResponse(
        Long id,
        String username,
        String displayName,
        boolean enabled,
        List<String> roleCodes
) {
}

