package com.derivops.mvp.auth.dto;
import com.derivops.mvp.auth.api.*;
import com.derivops.mvp.auth.application.*;


import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank String username,
        @NotBlank String password
) {
}
