package eu.dec21.appointme.categories.config;

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
                title = "AppointMe Categories API",
                description = "Comprehensive REST API for managing hierarchical business categories in the AppointMe appointment management system. " +
                        "This API provides endpoints for category CRUD operations, hierarchy navigation (parent-child relationships), " +
                        "and subcategory retrieval. Categories support unlimited depth with circular reference protection and maximum depth validation. " +
                        "Public endpoints return only active categories, while admin endpoints provide full access including inactive categories.",
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
                        url = "http://localhost:8081"
                ),
                @Server(
                        description = "Staging Environment",
                        url = "https://staging-categories.appointme.eu"
                ),
                @Server(
                        description = "Production Environment",
                        url = "https://categories.appointme.eu"
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
