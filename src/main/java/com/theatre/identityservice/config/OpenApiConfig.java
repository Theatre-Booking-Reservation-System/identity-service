package com.theatre.identityservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Central OpenAPI (Swagger) configuration for the Identity Service.
 *
 * <p>Exposes interactive API documentation at {@code /swagger-ui.html} and the raw
 * OpenAPI 3 definition at {@code /v3/api-docs}. A bearer (JWT) security scheme is
 * registered so protected endpoints can be exercised from the Swagger UI.
 */
@Configuration
public class OpenApiConfig {

    /** Name of the reusable bearer-token security scheme referenced by secured operations. */
    public static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI identityServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Identity Service API")
                        .description("Authentication and patron account management for the "
                                + "Theatre Booking & Reservation System. Issues JWT access tokens "
                                + "used by the other services.")
                        .version("v1")
                        .contact(new Contact().name("Theatre Platform Team"))
                        .license(new License().name("Apache 2.0")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local development")))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME,
                        new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Provide the JWT access token returned by "
                                        + "POST /auth/login using the format: Bearer <token>")));
    }
}
