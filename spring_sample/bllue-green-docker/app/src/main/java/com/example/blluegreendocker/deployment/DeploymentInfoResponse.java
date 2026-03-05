package com.example.blluegreendocker.deployment;

public record DeploymentInfoResponse(
        String application,
        String color,
        String version,
        long timestamp
) {
}
