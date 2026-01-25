package eu.dec21.appointme.categories.categories.entity;

import eu.dec21.appointme.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

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
                @Index(columnList = "parent_id")
        }
)
public class Category extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String name;

    private String description;
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
