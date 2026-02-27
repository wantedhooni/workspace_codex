package com.quant.portal.api.presentation.dto.adminuser;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record AdminUserUpdateRequest(
        @NotBlank String displayName,
        @NotEmpty List<@NotBlank String> roleCodes,
        boolean enabled,
        String newPassword
) {
}

