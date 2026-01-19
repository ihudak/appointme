package eu.dec21.appointme.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Keyword extends BaseEntity {
    @Column(name = "keyword", nullable = false, length = 128)
    private String keyword;

    @Column(name = "locale", length = 16)
    private String locale;

    @Column(name = "weight", nullable = false)
    private int weight = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 16)
    private Source source = Source.MANUAL;

    public enum Source {
        MANUAL,
        CATEGORY_SYNC,
        SYNONYM,
        SYSTEM
    }
}
