package com.example.samplebatchquartzdashboard.dashboard;

public record TaskSeedRequest(
        int size,
        boolean truncateBeforeLoad
) {
}
