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
                title = "QA Test Gym Booking API",
                version = "1.0",
                description = """
                        Backend for practicing automated testing scenarios around gym-style booking workflows.

                        Domain:
                        - Users: authentication + business role (USER / TRAINER / ADMIN)
                        - Courses: types of activities (e.g. Yoga, Boxing), ADMIN-only writes
                        - Sessions: scheduled occurrences of courses, ADMIN-only writes
                        - Bookings: reservations of sessions by users, subject to business rules

                        Authentication:
                        - HTTP Basic Auth (email + password) OR
                        - Bearer JWT token (obtained via /api/auth/login)

                        Authorization:
                        - USER: may view courses/sessions and book sessions (up to capacity)
                        - TRAINER: same as USER; may be assigned to run Courses/Sessions
                        - ADMIN: may manage users, courses, sessions and override bookings

                        Reference:
                        Detailed business rules are documented in RULES.md and enforced by the service layer.
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
                // Global security: every endpoint requires either Basic or Bearer
                // unless an operation explicitly sets security = {}.
                .addSecurityItem(new SecurityRequirement().addList("BasicAuth"))
                .addSecurityItem(new SecurityRequirement().addList("BearerAuth"));
    }
}
