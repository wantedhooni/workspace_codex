package com.quant.mvp.pipeline.payload;

import com.quant.mvp.pipeline.domain.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.List;

public final class CreateUserPayload {

    private CreateUserPayload() {
    }

    public record Req(
            @NotBlank @Email String email,
            @NotBlank String name,
            @NotNull UserStatus status,
            @NotEmpty List<@NotBlank String> roleCodes
    ) {
    }

    public record Res(
            Long userId,
            String email,
            String name,
            UserStatus status,
            List<String> roleCodes,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
