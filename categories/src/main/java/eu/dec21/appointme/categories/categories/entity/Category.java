package eu.dec21.appointme.categories.categories.entity;

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

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "categories",
        indexes = {
                @Index(name = "idx_categories_parent_id", columnList = "parent_id"),
                @Index(name = "idx_categories_active", columnList = "active")
        }
)
public class Category extends BaseEntity {

    @NotBlank(message = "Category name cannot be blank")
    @Size(min = 1, max = 100, message = "Category name must be 1-100 characters")
    @Column(unique = true, nullable = false, length = 100)
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    @Column(length = 500)
    private String description;

    @Size(max = 2048, message = "Image URL must not exceed 2048 characters")
    @URL(message = "Invalid image URL")
    @Column(length = 2048)
    private String imageUrl;

    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "parent_id", // nullable => root category when null
            foreignKey = @ForeignKey
    )
    private Category parent;

    @OneToMany(mappedBy = "parent")
    private Set<Category> children = new LinkedHashSet<>();

    @OneToMany(
            mappedBy = "category",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<CategoryKeyword> keywords = new LinkedHashSet<>();
}
