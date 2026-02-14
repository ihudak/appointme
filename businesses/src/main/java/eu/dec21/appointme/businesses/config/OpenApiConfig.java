package eu.dec21.appointme.businesses.config;

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
                title = "AppointMe Businesses API",
                description = "Comprehensive REST API for managing businesses, locations, and ratings in the AppointMe appointment management system. " +
                        "This API provides endpoints for business CRUD operations, category filtering, location-based search (PostGIS), " +
                        "and rating management. Public endpoints return only active businesses, while authenticated owner and admin endpoints " +
                        "provide full access to business management.",
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
                        url = "http://localhost:8080"
                ),
                @Server(
                        description = "Staging Environment",
                        url = "https://staging-api.appointme.eu"
                ),
                @Server(
                        description = "Production Environment",
                        url = "https://api.appointme.eu"
                )
        }
)
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Bearer token authentication. Obtain a token by calling POST /auth/authenticate with valid credentials. " +
                "The token must be included in the Authorization header as 'Bearer {token}' for all protected endpoints. " +
                "Tokens expire after 24 hours and must be refreshed.",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
}
