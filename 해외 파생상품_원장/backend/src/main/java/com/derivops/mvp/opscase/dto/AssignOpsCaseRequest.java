package com.derivops.mvp.opscase.dto;
import com.derivops.mvp.opscase.*;
import com.derivops.mvp.opscase.api.*;
import com.derivops.mvp.opscase.application.*;
import com.derivops.mvp.opscase.infrastructure.*;


import jakarta.validation.constraints.NotBlank;

public record AssignOpsCaseRequest(
        @NotBlank String assignee,
        @NotBlank String comment
) {
}
