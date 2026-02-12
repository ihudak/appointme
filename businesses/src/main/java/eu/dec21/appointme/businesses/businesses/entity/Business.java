package eu.dec21.appointme.businesses.businesses.entity;

import eu.dec21.appointme.common.entity.Address;
import eu.dec21.appointme.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.URL;
import org.locationtech.jts.geom.Point;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "businesses",
        indexes = {
                @Index(name = "idx_businesses_rating", columnList = "rating"),
                @Index(name = "idx_businesses_weighted_rating", columnList = "weightedRating"),
                @Index(name = "idx_businesses_name", columnList = "name"),
                @Index(name = "idx_businesses_owner_id", columnList = "ownerId"),
                @Index(name = "idx_businesses_active", columnList = "active")
        }
)
public class Business extends BaseEntity {

    @NotBlank(message = "Business name cannot be blank")
    @Size(min = 1, max = 255, message = "Business name must be 1-255 characters")
    @Column(nullable = false, length = 255)
    private String name;

    @Size(max = 2000, message = "Description must not exceed 2000 characters")
    @Column(length = 2000)
    private String description;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;

    @Embedded
    private Address address;

    @Column(columnDefinition = "geography(Point,4326)", nullable = false)
    private Point location;

    @Pattern(
            regexp = "^(\\+[1-9]\\d{1,14})?$",
            message = "Invalid phone number (expected E.164, e.g. +4915112345678)"
    )
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    @Column(length = 20)
    private String phoneNumber;

    @URL(
            protocol = "https",
            regexp = "^(https?)://.+$",
            message = "Invalid website URL (expected http(s)://...)"
    )
    @Size(max = 2048, message = "Website URL must not exceed 2048 characters")
    @Column(length = 2048)
    private String website;

    @Email(message = "Invalid email address")
    @NotBlank(message = "Email cannot be blank")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean emailVerified;

    @Column(nullable = false)
    private Long ownerId;

    private Double rating;
    private Integer reviewCount;
    private Double weightedRating;

    @ElementCollection
    @CollectionTable(
            name = "business_category_ids",
            joinColumns = @JoinColumn(
                    name = "business_id",
                    nullable = false,
                    foreignKey = @ForeignKey
            ),
            uniqueConstraints = @UniqueConstraint(columnNames = {"category_id", "business_id"}),
            indexes = @Index(columnList = "business_id")
    )
    @Column(name = "category_id", nullable = false)
    @Builder.Default
    private Set<Long> categoryIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "business_admin_ids",
            joinColumns = @JoinColumn(
                    name = "business_id",
                    nullable = false,
                    foreignKey = @ForeignKey
            ),
            uniqueConstraints = @UniqueConstraint(columnNames = {"admin_id", "business_id"}),
            indexes = @Index(columnList = "business_id")
    )
    @Column(name = "admin_id", nullable = false)
    @Builder.Default
    private Set<Long> adminIds = new HashSet<>();

    @OneToMany(
            mappedBy = "business",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<BusinessKeyword> keywords = new HashSet<>();

    @OneToMany(
            mappedBy = "business",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private Set<BusinessImage> images = new HashSet<>();

    @Transient
    public Double getCalculatedRating(int confidenceThreshold, double globalMean) {
        if (rating == null || reviewCount == null || reviewCount == 0) {
            return 0.0;
        }
        // Bayesian average: (C × m + n × r) / (C + n)
        // C = confidence threshold, m = global mean, n = review count, r = average rating
        return (confidenceThreshold * globalMean + reviewCount * rating) / (confidenceThreshold + reviewCount);
    }

    public void updateRating(Double newRating, Integer newReviewCount, int confidenceThreshold, double globalMean) {
        this.rating = newRating;
        this.reviewCount = newReviewCount;
        this.weightedRating = getCalculatedRating(confidenceThreshold, globalMean);
    }
}
