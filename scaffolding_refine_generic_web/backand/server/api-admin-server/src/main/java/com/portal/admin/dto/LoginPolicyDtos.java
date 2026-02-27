package com.portal.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class LoginPolicyDtos {
    private LoginPolicyDtos() {}

    @Schema(name = "CreateLoginPolicyRequest", description = "Login policy creation payload")
    public record CreateLoginPolicyRequest(
            @Schema(description = "Policy name", example = "Default Login Policy")
            @NotBlank String name,
            @Schema(description = "Max fail count", example = "5")
            @NotNull Integer maxFailCount,
            @Schema(description = "Lock minutes", example = "30")
            @NotNull Integer lockMinutes,
            @Schema(description = "Allowed IP CIDR", example = "0.0.0.0/0")
            String allowedIpCidr,
            @Schema(description = "Enabled")
            Boolean enabled
    ) {}

    @Schema(name = "UpdateLoginPolicyRequest", description = "Login policy update payload")
    public record UpdateLoginPolicyRequest(
            @Schema(description = "Policy name")
            String name,
            @Schema(description = "Max fail count")
            Integer maxFailCount,
            @Schema(description = "Lock minutes")
            Integer lockMinutes,
            @Schema(description = "Allowed IP CIDR")
            String allowedIpCidr,
            @Schema(description = "Enabled")
            Boolean enabled
    ) {}

    @Schema(name = "LoginPolicyResponse", description = "Login policy response")
    public record LoginPolicyResponse(
            @Schema(description = "ID")
            Long id,
            @Schema(description = "Policy name")
            String name,
            @Schema(description = "Max fail count")
            Integer maxFailCount,
            @Schema(description = "Lock minutes")
            Integer lockMinutes,
            @Schema(description = "Allowed IP CIDR")
            String allowedIpCidr,
            @Schema(description = "Enabled")
            Boolean enabled
    ) {}
}
