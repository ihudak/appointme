package eu.dec21.appointme.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
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
    @Column(name = "formatted_address", length = 512)
    private String formattedAddress;

    @Column(name = "address_line1", length = 255)
    private String line1;

    @Column(name = "address_line2", length = 255)
    private String line2;

    @Column(name = "locality", length = 128) // city/town
    private String city;

    @Column(name = "administrative_area", length = 128) // state/region
    private String region;

    @Column(name = "postal_code", length = 32)
    private String postalCode;

    @Column(name = "country_code", length = 2) // ISO-3166-1 alpha-2, e.g. "US"
    private String countryCode;

    // Optional: provider-specific stable identifier (Google PlaceId, OSM place_id, etc.)
    @Column(name = "place_id", length = 128)
    private String placeId;
}
