package eu.dec21.appointme.businesses.businesses.response;

import eu.dec21.appointme.common.entity.Address;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Business response with all business details including ratings")
public class BusinessResponse {
    @Schema(description = "Unique business identifier", example = "123")
    private Long id;
    
    @Schema(description = "Business name", example = "Bella Italia Restaurant")
    private String name;
    
    @Schema(description = "Detailed business description", example = "Authentic Italian cuisine with homemade pasta and wood-fired pizza")
    private String description;
    
    @Schema(description = "Business physical address", implementation = Address.class)
    private Address address;
    
    @Schema(description = "Geographic coordinates (PostGIS Point)", example = "{\"type\": \"Point\", \"coordinates\": [13.405, 52.52]}")
    private Point location;
    
    @Schema(description = "Business contact phone number", example = "+49 30 12345678")
    private String phoneNumber;
    
    @Schema(description = "Business website URL", example = "https://www.bella-italia.de")
    private String website;
    
    @Schema(description = "Business contact email address", example = "info@bella-italia.de")
    private String email;
    
    @Schema(description = "Business profile image URL", example = "https://storage.appointme.eu/businesses/123/profile.jpg")
    private String imageUrl;
    
    @Schema(description = "Average customer rating (0.0 to 5.0)", example = "4.5", minimum = "0", maximum = "5")
    private Double rating;
    
    @Schema(description = "Total number of customer reviews", example = "127")
    private Integer reviewCount;
    
    @Schema(description = "Whether the business is currently active and visible to public", example = "true")
    private boolean active;
}
