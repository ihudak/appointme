package eu.dec21.appointme.common.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class KeywordTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // ===== Constructor and Builder Tests =====

    @Test
    void testBuilder_withAllFields() {
        Keyword keyword = Keyword.builder()
                .keyword("search term")
                .locale("en-US")
                .weight(150)
                .source(Keyword.Source.MANUAL)
                .build();

        assertEquals("search term", keyword.getKeyword());
        assertEquals("en-US", keyword.getLocale());
        assertEquals(150, keyword.getWeight());
        assertEquals(Keyword.Source.MANUAL, keyword.getSource());
    }

    @Test
    void testBuilder_withMinimalFields() {
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .build();

        assertEquals("test", keyword.getKeyword());
        assertNull(keyword.getLocale());
        assertEquals(100, keyword.getWeight()); // Default value
        assertEquals(Keyword.Source.MANUAL, keyword.getSource()); // Default value
    }

    @Test
    void testNoArgsConstructor() {
        Keyword keyword = new Keyword();

        assertNull(keyword.getKeyword());
        assertNull(keyword.getLocale());
        assertEquals(100, keyword.getWeight()); // Default from field initialization
        assertEquals(Keyword.Source.MANUAL, keyword.getSource()); // Default from field initialization
    }

    @Test
    void testAllArgsConstructor() {
        Keyword keyword = new Keyword("electronics", "de", 200, Keyword.Source.CATEGORY_SYNC);

        assertEquals("electronics", keyword.getKeyword());
        assertEquals("de", keyword.getLocale());
        assertEquals(200, keyword.getWeight());
        assertEquals(Keyword.Source.CATEGORY_SYNC, keyword.getSource());
    }

    @Test
    void testDefaultValues() {
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .build();

        assertEquals(100, keyword.getWeight(), "Default weight should be 100");
        assertEquals(Keyword.Source.MANUAL, keyword.getSource(), "Default source should be MANUAL");
    }

    // ===== Keyword Field Validation Tests =====

    @Test
    void testKeyword_withValidValue() {
        Keyword keyword = Keyword.builder()
                .keyword("electronics")
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testKeyword_withMinLength() {
        Keyword keyword = Keyword.builder()
                .keyword("a")
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testKeyword_withMaxLength() {
        String maxLengthKeyword = "k".repeat(128);
        Keyword keyword = Keyword.builder()
                .keyword(maxLengthKeyword)
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testKeyword_exceedsMaxLength() {
        String tooLongKeyword = "k".repeat(129);
        Keyword keyword = Keyword.builder()
                .keyword(tooLongKeyword)
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("keyword") &&
                        v.getMessage().contains("1-128 characters")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void testKeyword_blankOrEmpty(String keyword) {
        Keyword kw = Keyword.builder()
                .keyword(keyword)
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(kw);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("keyword")));
    }

    @Test
    void testKeyword_withSpecialCharacters() {
        Keyword keyword = Keyword.builder()
                .keyword("C++ programming")
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testKeyword_withUnicode() {
        Keyword keyword = Keyword.builder()
                .keyword("プログラミング")
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

    // ===== Locale Field Validation Tests =====

    @ParameterizedTest
    @ValueSource(strings = {
            "en",
            "de",
            "fr",
            "es",
            "zh",
            "ja",
            "en-US",
            "en-GB",
            "de-DE",
            "zh-CN",
            "pt-BR"
    })
    void testLocale_withValidValues(String locale) {
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .locale(locale)
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLocale_withMaxLength() {
        String maxLengthLocale = "l".repeat(16);
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .locale(maxLengthLocale)
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLocale_exceedsMaxLength() {
        String tooLongLocale = "l".repeat(17);
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .locale(tooLongLocale)
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("locale")));
    }

    @Test
    void testLocale_nullIsValid() {
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .locale(null)
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

    // ===== Weight Field Validation Tests =====

    @Test
    void testWeight_withMinValue() {
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .weight(0)
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testWeight_withMaxValue() {
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .weight(1000)
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testWeight_belowMinValue() {
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .weight(-1)
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("weight") &&
                        v.getMessage().contains("must be at least 0")));
    }

    @Test
    void testWeight_exceedsMaxValue() {
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .weight(1001)
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("weight") &&
                        v.getMessage().contains("must not exceed 1000")));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 50, 100, 150, 500, 999, 1000})
    void testWeight_withValidValues(int weight) {
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .weight(weight)
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

    // ===== Source Enum Tests =====

    @ParameterizedTest
    @EnumSource(Keyword.Source.class)
    void testSource_allEnumValues(Keyword.Source source) {
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .source(source)
                .build();

        assertEquals(source, keyword.getSource());
        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testSource_manualDefault() {
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .build();

        assertEquals(Keyword.Source.MANUAL, keyword.getSource());
    }

    @Test
    void testSource_categorySyncValue() {
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .source(Keyword.Source.CATEGORY_SYNC)
                .build();

        assertEquals(Keyword.Source.CATEGORY_SYNC, keyword.getSource());
    }

    @Test
    void testSource_synonymValue() {
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .source(Keyword.Source.SYNONYM)
                .build();

        assertEquals(Keyword.Source.SYNONYM, keyword.getSource());
    }

    @Test
    void testSource_systemValue() {
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .source(Keyword.Source.SYSTEM)
                .build();

        assertEquals(Keyword.Source.SYSTEM, keyword.getSource());
    }

    // ===== Setter Tests =====

    @Test
    void testSetters() {
        Keyword keyword = new Keyword();

        keyword.setKeyword("updated keyword");
        keyword.setLocale("fr-FR");
        keyword.setWeight(250);
        keyword.setSource(Keyword.Source.SYNONYM);

        assertEquals("updated keyword", keyword.getKeyword());
        assertEquals("fr-FR", keyword.getLocale());
        assertEquals(250, keyword.getWeight());
        assertEquals(Keyword.Source.SYNONYM, keyword.getSource());
    }

    // ===== Inheritance Tests (extends BaseEntity) =====

    @Test
    void testBaseEntity_inheritance() {
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .build();

        // Verify inherited fields from BaseEntity are accessible
        assertNull(keyword.getId()); // From BaseBasicEntity
        assertNull(keyword.getCreatedAt()); // From BaseBasicEntity
        assertNull(keyword.getUpdatedAt()); // From BaseBasicEntity
        assertNull(keyword.getCreatedBy()); // From BaseEntity
        assertNull(keyword.getUpdatedBy()); // From BaseEntity
    }

    @Test
    void testKeyword_setInheritedFields() {
        Keyword keyword = Keyword.builder()
                .keyword("test")
                .build();

        keyword.setId(123L);
        keyword.setCreatedBy(456L);
        keyword.setUpdatedBy(789L);

        assertEquals(123L, keyword.getId());
        assertEquals(456L, keyword.getCreatedBy());
        assertEquals(789L, keyword.getUpdatedBy());
    }

    // ===== Complete Keyword Tests =====

    @Test
    void testCompleteKeyword_allFieldsValid() {
        Keyword keyword = Keyword.builder()
                .keyword("machine learning")
                .locale("en-US")
                .weight(500)
                .source(Keyword.Source.CATEGORY_SYNC)
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());

        assertEquals("machine learning", keyword.getKeyword());
        assertEquals("en-US", keyword.getLocale());
        assertEquals(500, keyword.getWeight());
        assertEquals(Keyword.Source.CATEGORY_SYNC, keyword.getSource());
    }

    @Test
    void testMultipleViolations() {
        String tooLongKeyword = "k".repeat(129);
        String tooLongLocale = "l".repeat(17);

        Keyword keyword = Keyword.builder()
                .keyword(tooLongKeyword)
                .locale(tooLongLocale)
                .weight(-10)
                .build();

        Set<ConstraintViolation<Keyword>> violations = validator.validate(keyword);
        assertFalse(violations.isEmpty());
        assertEquals(3, violations.size());

        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("keyword")));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("locale")));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("weight")));
    }

    @Test
    void testKeyword_commonUseCases() {
        // Scenario 1: SEO keyword
        Keyword seoKeyword = Keyword.builder()
                .keyword("best laptop 2024")
                .locale("en")
                .weight(200)
                .source(Keyword.Source.MANUAL)
                .build();
        assertTrue(validator.validate(seoKeyword).isEmpty());

        // Scenario 2: Category sync keyword
        Keyword categoryKeyword = Keyword.builder()
                .keyword("electronics")
                .locale("en")
                .weight(150)
                .source(Keyword.Source.CATEGORY_SYNC)
                .build();
        assertTrue(validator.validate(categoryKeyword).isEmpty());

        // Scenario 3: Synonym keyword
        Keyword synonymKeyword = Keyword.builder()
                .keyword("laptop")
                .locale("en")
                .weight(100)
                .source(Keyword.Source.SYNONYM)
                .build();
        assertTrue(validator.validate(synonymKeyword).isEmpty());

        // Scenario 4: System-generated keyword
        Keyword systemKeyword = Keyword.builder()
                .keyword("tech gadgets")
                .locale("en-US")
                .weight(50)
                .source(Keyword.Source.SYSTEM)
                .build();
        assertTrue(validator.validate(systemKeyword).isEmpty());
    }
}
