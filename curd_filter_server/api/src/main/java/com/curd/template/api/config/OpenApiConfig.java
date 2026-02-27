package com.curd.template.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI crudTemplateOpenApi() {
        return new OpenAPI()
            .addServersItem(new Server().url("/").description("Default server"))
            .info(new Info()
                .title("Enterprise CRUD Filter Template API")
                .description("Spring Boot 4 + JPA + Querydsl CRUD template with filter policy control")
                .version("v1")
                .contact(new Contact().name("Platform Team").email("platform@example.com"))
            );
    }
}
