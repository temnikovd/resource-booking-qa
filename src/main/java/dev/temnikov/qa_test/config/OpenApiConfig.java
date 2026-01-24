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
                        Backend for practicing automated testing scenarios around booking workflows.
                        
                        Domain:
                        - Users: registration, role management (USER / ADMIN).
                        - Resources: bookable entities (rooms, courts, etc.), ADMIN-only writes.
                        - Slots: time ranges attached to resources, ADMIN-only writes, non-overlapping and future-only.
                        - Bookings: reservations of slots by users, subject to business rules.
                        
                        Authentication:
                        - HTTP Basic Auth (email + password) OR
                        - Bearer JWT token (obtained via /api/auth/login).
                        
                        Authorization:
                        - USER: can view resources/slots and manage own bookings.
                        - ADMIN: can manage users, resources, slots and create/cancel bookings for any user.
                        
                        Detailed business rules are described in RULES.md and are enforced by the service layer.
                        """
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "Local development server")
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
                // Global security: every endpoint requires either Basic or Bearer,
                // unless an operation explicitly overrides security = {}.
                .addSecurityItem(new SecurityRequirement().addList("BasicAuth"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }
}
