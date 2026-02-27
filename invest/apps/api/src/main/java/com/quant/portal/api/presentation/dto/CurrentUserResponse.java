package com.quant.portal.api.presentation.dto;

import java.util.List;

public record CurrentUserResponse(
        String username,
        List<String> roles
) {
}
