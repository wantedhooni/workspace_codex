package com.example.samplebatchquartzdashboard.dashboard;

public record TaskJobLaunchResponse(
        Long jobExecutionId,
        String status
) {
}
