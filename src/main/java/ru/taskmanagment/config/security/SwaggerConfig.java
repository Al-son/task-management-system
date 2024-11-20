package ru.taskmanagment.config.security;


import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi productApi() {
        return GroupedOpenApi.builder()
                .group("taskManagement")
                .packagesToScan("ru.taskmanagment.controller")
                .pathsToMatch("/**")
                .addOpenApiCustomizer(openApi -> openApi
                        .info(new Info().title("taskManagement-api").version("v1")))
                .build();
    }
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("taskManagement-api")
                        .description("taskManagement-service app openAPI")
                        .contact(new Contact().name("Support").email("support@alson.ru"))
                        .version("v1.0.0")
                        .license(new License().name("1.0.0").url("http://alson.ru"))
                )
                .components(new io.swagger.v3.oas.models.Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .externalDocs(new ExternalDocumentation()
                        .description("v Wiki Documentation")
                        .url("http://alson.ru")
                );
    }

}
