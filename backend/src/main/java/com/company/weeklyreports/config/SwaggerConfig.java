package com.company.weeklyreports.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Defines the OpenAPI metadata SpringDoc uses to generate Swagger UI
 * (/swagger-ui.html) and the raw spec (/v3/api-docs) directly from the
 * existing @RestController classes and their @Tag/@Operation/@ApiResponse
 * annotations - there is no separate hand-written spec file to keep in
 * sync with the code.
 */
@Configuration
public class SwaggerConfig {

    private static final String BEARER_SCHEME_NAME = "bearerAuth";

    // Registers one "bearerAuth" HTTP-bearer security scheme and applies
    // it globally via addSecurityItem, so every endpoint's page in Swagger
    // UI shows a padlock and the top-level "Authorize" button accepts a
    // raw token. Swagger UI itself prepends "Bearer " before sending the
    // Authorization header on "Try it out" calls, so whoever is using the
    // docs pastes in only the token value - no "Bearer " prefix needed.
    @Bean
    public OpenAPI weeklyReportGeneratorOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Weekly Report Generator API")
                        .version("1.0")
                        .description("REST API for submitting, reviewing, and reporting on weekly team reports."))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME_NAME, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
