package eu.dec21.appointme.users.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "AppointMe Users & Authentication API",
                description = "Comprehensive REST API for user management and authentication in the AppointMe appointment management system. " +
                        "This API provides endpoints for user registration with email verification, JWT-based authentication, " +
                        "account activation, role-based access control (RBAC), and group management. " +
                        "Authentication flow: register → verify email → authenticate → receive JWT token → use token for protected endpoints.",
                version = "1.0.0",
                contact = @Contact(
                        name = "AppointMe Development Team",
                        email = "support@appointme.eu",
                        url = "https://appointme.eu"
                ),
                license = @License(
                        name = "MIT License",
                        url = "https://opensource.org/licenses/MIT"
                )
        ),
        servers = {
                @Server(
                        description = "Local Development Server",
                        url = "http://localhost:8082"
                ),
                @Server(
                        description = "Staging Environment",
                        url = "https://staging-auth.appointme.eu"
                ),
                @Server(
                        description = "Production Environment",
                        url = "https://auth.appointme.eu"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Bearer token authentication. Obtain a token by calling POST /auth/authenticate with valid credentials " +
                "after completing the registration and email verification process. " +
                "The token must be included in the Authorization header as 'Bearer {token}' for all protected endpoints. " +
                "Tokens contain user claims (email, roles, authorities) and expire after 24 hours. " +
                "Token format: eyJhbGciOiJIUzI1NiJ9.{payload}.{signature}",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
