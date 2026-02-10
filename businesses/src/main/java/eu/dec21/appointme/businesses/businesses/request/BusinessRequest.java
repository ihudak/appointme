package eu.dec21.appointme.businesses.businesses.request;

import eu.dec21.appointme.common.entity.Address;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.locationtech.jts.geom.Point;

public record BusinessRequest(
        Long id,
        @NotBlank(message = "Name cannot be blank")
        String name,
        String description,
        Address address,
        Point location,
        String phoneNumber,
        String website,
        String email
) {
}
