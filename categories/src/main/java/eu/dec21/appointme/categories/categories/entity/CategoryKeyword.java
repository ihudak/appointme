package eu.dec21.appointme.categories.categories.entity;

import eu.dec21.appointme.common.entity.Keyword;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "category_keywords",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_category_keyword_locale", columnNames = {"category_id", "keyword", "locale"})
        },
        indexes = {
                @Index(name = "idx_category_keywords_keyword", columnList = "keyword"),
                @Index(name = "idx_category_keywords_locale", columnList = "locale"),
                @Index(name = "idx_category_keywords_category_id", columnList = "category_id")
        }
)
public class CategoryKeyword extends Keyword {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "category_id",
            nullable = false,
            foreignKey = @ForeignKey
    )
    private Category category;
}