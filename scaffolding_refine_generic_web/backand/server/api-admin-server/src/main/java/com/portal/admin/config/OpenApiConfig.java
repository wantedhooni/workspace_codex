package com.portal.admin.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Portal Admin API",
                version = "v1",
                description = "Admin portal CRUD API",
                license = @License(name = "Apache-2.0")
        )
)
public class OpenApiConfig {
}
