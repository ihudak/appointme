package eu.dec21.appointme.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    // Full address string as returned by the geocoder (display + audit)
    @Size(max = 512, message = "Formatted address must not exceed 512 characters")
    @Column(name = "formatted_address", length = 512)
    private String formattedAddress;

    @Size(max = 255, message = "Address line 1 must not exceed 255 characters")
    @Column(name = "address_line1", length = 255)
    private String line1;

    @Size(max = 255, message = "Address line 2 must not exceed 255 characters")
    @Column(name = "address_line2", length = 255)
    private String line2;

    @Size(max = 128, message = "City must not exceed 128 characters")
    @Column(name = "locality", length = 128) // city/town
    private String city;

    @Size(max = 128, message = "Region must not exceed 128 characters")
    @Column(name = "administrative_area", length = 128) // state/region
    private String region;

    @Size(max = 32, message = "Postal code must not exceed 32 characters")
    @Column(name = "postal_code", length = 32)
    private String postalCode;

    @Size(min = 2, max = 2, message = "Country code must be exactly 2 characters (ISO-3166-1 alpha-2)")
    @Pattern(regexp = "^[A-Z]{2}$", message = "Country code must be 2 uppercase letters (e.g., US, DE, GB)")
    @Column(name = "country_code", length = 2) // ISO-3166-1 alpha-2, e.g. "US"
    private String countryCode;

    // Optional: provider-specific stable identifier (Google PlaceId, OSM place_id, etc.)
    @Size(max = 128, message = "Place ID must not exceed 128 characters")
    @Column(name = "place_id", length = 128)
    private String placeId;
}
