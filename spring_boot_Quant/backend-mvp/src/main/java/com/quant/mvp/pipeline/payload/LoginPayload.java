package com.quant.mvp.pipeline.payload;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public class LoginPayload {

    public record Req(
            @NotBlank(message = "email is required")
            String email,
            @NotBlank(message = "password is required")
            String password
    ) {
    }

    public record Res(
            String accessToken,
            String tokenType,
            long expiresInSeconds,
            Long userId,
            String email,
            String name,
            List<String> roleCodes
    ) {
    }
}
