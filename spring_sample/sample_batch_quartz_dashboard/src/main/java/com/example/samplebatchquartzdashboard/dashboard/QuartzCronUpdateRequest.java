package com.example.samplebatchquartzdashboard.dashboard;

import jakarta.validation.constraints.NotBlank;

public record QuartzCronUpdateRequest(
        @NotBlank String cronExpression
) {
}
