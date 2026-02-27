package com.quant.portal.api.presentation.dto.adminuser;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AdminUserCreateRequest(
        @NotBlank String username,
        @NotBlank String password,
        @NotBlank String displayName,
        @NotEmpty List<@NotBlank String> roleCodes,
        Boolean enabled
) {
}

