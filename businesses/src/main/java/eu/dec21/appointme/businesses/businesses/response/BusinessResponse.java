package eu.dec21.appointme.businesses.businesses.response;

import eu.dec21.appointme.common.entity.Address;
import lombok.*;
import org.locationtech.jts.geom.Point;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessResponse {
    private Long id;
    private String name;
    private String description;
    private Address address;
    private Point location;
    private String phoneNumber;
    private String website;
    private String email;
    private String imageUrl;
    private Double rating;
    private Integer reviewCount;
    private boolean active;
}
