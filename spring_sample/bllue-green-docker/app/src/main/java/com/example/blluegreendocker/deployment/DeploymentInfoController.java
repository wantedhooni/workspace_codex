package com.example.blluegreendocker.deployment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/deployment")
public class DeploymentInfoController {

    private final String color;
    private final String version;

    public DeploymentInfoController(
            @Value("${deploy.color:unknown}") String color,
            @Value("${deploy.version:local}") String version
    ) {
        this.color = color;
        this.version = version;
    }

    @GetMapping
    public DeploymentInfoResponse deploymentInfo() {
        return new DeploymentInfoResponse(
                "bllue-green-docker-app",
                color,
                version,
                System.currentTimeMillis()
        );
    }
}
