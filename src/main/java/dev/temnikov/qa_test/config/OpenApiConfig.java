package dev.temnikov.qa_test.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "QA Test Slot Booking API",
                version = "1.0",
                description = """
                        Backend for practicing automated testing scenarios.

                        The API exposes user, resource, slot and booking endpoints.
                        Detailed business rules are described separately in RULES.md.
                        """
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local server")
        }
)
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        SecurityScheme basicAuthScheme = new SecurityScheme()
                .name("BasicAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("basic");

        SecurityScheme bearerAuthScheme = new SecurityScheme()
                .name("BearerAuth")
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT");

        return new OpenAPI()
                .components(new Components()
                        .addSecuritySchemes("BasicAuth", basicAuthScheme)
                        .addSecuritySchemes("BearerAuth", bearerAuthScheme)
                )
                // These entries represent alternative security mechanisms (Basic OR Bearer).
                .addSecurityItem(new SecurityRequirement().addList("BasicAuth"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }
}
