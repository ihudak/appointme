package eu.dec21.appointme.businesses.businesses.entity;

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
        name = "business_keywords",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uq_business_keyword_type_locale",
                        columnNames = {"business_id", "keyword", "type", "locale"}
                )
        },
        indexes = {
                @Index(name = "idx_business_keywords_keyword", columnList = "keyword"),
                @Index(name = "idx_business_keywords_locale", columnList = "locale"),
                @Index(name = "idx_business_keywords_type", columnList = "type"),
                @Index(name = "idx_business_keywords_business_id", columnList = "business_id")
        }
)
public class BusinessKeyword extends Keyword {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "business_id",
            nullable = false,
            foreignKey = @ForeignKey
    )
    private Business business;
}
