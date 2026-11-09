package com.techservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    public static final String BEARER_SCHEME = "bearerAuth";

    @Bean
    public OpenAPI techServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TechService Manager API")
                        .description("""
                                API REST versionada (`/api/v1`) para la gestión integral de servicios \
                                técnicos: usuarios, clientes, técnicos, equipos y órdenes de servicio.

                                **Autenticación:** obtener token con `POST /api/v1/auth/login` y \
                                pulsar **Authorize** (Bearer JWT).

                                **Roles:** `ADMIN`, `TECNICO`, `CLIENTE`.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("TechService / essentialsplus.cloud")
                                .url("https://essentialsplus.cloud")
                                .email("soporte@essentialsplus.cloud"))
                        .license(new License().name("Proprietary").url("https://essentialsplus.cloud")))
                .externalDocs(new ExternalDocumentation()
                        .description("README del monorepo")
                        .url("https://github.com/"))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local (Maven / NetBeans)"),
                        new Server().url("http://localhost:8088").description("Docker (nginx proxy /api)")
                ))
                .tags(List.of(
                        new Tag().name("Auth").description("Login, refresh y perfil"),
                        new Tag().name("Usuarios"),
                        new Tag().name("Clientes"),
                        new Tag().name("Tecnicos"),
                        new Tag().name("Equipos"),
                        new Tag().name("Ordenes")
                ))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME))
                .components(new Components()
                        .addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                                .name(BEARER_SCHEME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Pegar el accessToken devuelto por /api/v1/auth/login")));
    }
}
