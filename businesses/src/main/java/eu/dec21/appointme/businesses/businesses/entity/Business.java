package eu.dec21.appointme.businesses.businesses.entity;

import eu.dec21.appointme.common.entity.Address;
import eu.dec21.appointme.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
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
@Table(name = "businesses")
public class Business extends BaseEntity {

    @Column(nullable = false)
    private String name;

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
    private String phoneNumber;

    @URL(
            protocol = "https",
            regexp = "^(https?)://.+$",
            message = "Invalid website URL (expected http(s)://...)"
    )
    private String website;

    @Email(message = "Invalid email address")
    @NotBlank(message = "Email cannot be blank")
    @Column(unique = true, nullable = false)
    private String email;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private boolean emailVerified;

    @Column(nullable = false)
    private Long owner;

    private Double rating;
    private Integer reviewCount;

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
    private Set<Long> adminIds = new HashSet<>();

    @OneToMany(
            mappedBy = "business",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<BusinessKeyword> keywords = new HashSet<>();

    @OneToMany(
            mappedBy = "business",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<BusinessImage> images = new HashSet<>();
}
