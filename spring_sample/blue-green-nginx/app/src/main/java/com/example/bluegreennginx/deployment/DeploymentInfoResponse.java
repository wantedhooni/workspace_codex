package com.example.bluegreennginx.deployment;

public record DeploymentInfoResponse(
        String application,
        String color,
        String version,
        long timestamp
) {
}
