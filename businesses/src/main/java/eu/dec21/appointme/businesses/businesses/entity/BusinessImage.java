package eu.dec21.appointme.businesses.businesses.entity;

import eu.dec21.appointme.common.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "business_images",
        indexes = {
                @Index(name = "idx_business_images_business_id", columnList = "business_id"),
                @Index(name = "idx_business_images_is_icon", columnList = "isIcon"),
                @Index(name = "idx_business_images_display_order", columnList = "displayOrder")
        }
)
public class BusinessImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "business_id",
            nullable = false,
            foreignKey = @ForeignKey
    )
    private Business business;

    @NotBlank(message = "Image URL cannot be blank")
    @URL(message = "Invalid image URL")
    @Size(max = 2048, message = "Image URL must not exceed 2048 characters")
    @Column(nullable = false, length = 2048)
    private String imageUrl;

    @Size(max = 500, message = "Alt text must not exceed 500 characters")
    @Column(length = 500)
    private String altText;

    @Column(nullable = false)
    private Integer displayOrder = 0;

    @Column(nullable = false)
    private Boolean isIcon = false;
}
