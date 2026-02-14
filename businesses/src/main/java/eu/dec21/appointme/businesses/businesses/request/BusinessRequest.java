package eu.dec21.appointme.businesses.businesses.request;

import eu.dec21.appointme.common.entity.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.locationtech.jts.geom.Point;

@Schema(description = "Request payload for creating or updating a business")
public record BusinessRequest(
        @Schema(description = "Business ID (only for updates, ignored during creation)", example = "123", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,
        
        @Schema(description = "Business name (required, unique within system)", example = "Bella Italia Restaurant", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "Name cannot be blank")
        @Size(max = 255, message = "Name must not exceed 255 characters")
        String name,
        
        @Schema(description = "Detailed business description", example = "Authentic Italian cuisine with homemade pasta and wood-fired pizza")
        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,
        
        @Schema(description = "Business physical address", implementation = Address.class)
        Address address,
        
        @Schema(description = "Geographic coordinates (PostGIS Point)", example = "{\"type\": \"Point\", \"coordinates\": [13.405, 52.52]}")
        Point location,
        
        @Schema(description = "Business contact phone number", example = "+49 30 12345678")
        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phoneNumber,
        
        @Schema(description = "Business website URL", example = "https://www.bella-italia.de")
        @Size(max = 500, message = "Website URL must not exceed 500 characters")
        String website,
        
        @Schema(description = "Business contact email address", example = "info@bella-italia.de")
        @Email(message = "Must be a valid email address")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email
) {
}
