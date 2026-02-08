package eu.dec21.appointme.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
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
    @NotBlank(message = "Keyword cannot be blank")
    @Size(min = 1, max = 128, message = "Keyword must be 1-128 characters")
    @Column(name = "keyword", nullable = false, length = 128)
    private String keyword;

    @Size(max = 16, message = "Locale must not exceed 16 characters")
    @Column(name = "locale", length = 16)
    private String locale;

    @Min(value = 0, message = "Weight must be at least 0")
    @Max(value = 1000, message = "Weight must not exceed 1000")
    @Column(name = "weight", nullable = false)
    @Builder.Default
    private int weight = 100;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 16)
    @Builder.Default
    private Source source = Source.MANUAL;

    public enum Source {
        MANUAL,
        CATEGORY_SYNC,
        SYNONYM,
        SYSTEM
    }
}
