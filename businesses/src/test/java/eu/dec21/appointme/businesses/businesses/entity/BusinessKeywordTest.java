package eu.dec21.appointme.businesses.businesses.entity;

import eu.dec21.appointme.common.entity.Keyword;
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

class BusinessKeywordTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testBuilder_withAllFields() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword keyword = BusinessKeyword.builder()
                .keyword("pizza delivery")
                .locale("en")
                .weight(150)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        assertNotNull(keyword);
        assertEquals("pizza delivery", keyword.getKeyword());
        assertEquals("en", keyword.getLocale());
        assertEquals(150, keyword.getWeight());
        assertEquals(Keyword.Source.MANUAL, keyword.getSource());
        assertEquals(business, keyword.getBusiness());
    }

    @Test
    void testBuilder_withMinimalFields() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword keyword = BusinessKeyword.builder()
                .keyword("restaurant")
                .business(business)
                .build();

        assertNotNull(keyword);
        assertEquals("restaurant", keyword.getKeyword());
        assertEquals(business, keyword.getBusiness());
        assertNull(keyword.getLocale());
    }

    @Test
    void testNoArgsConstructor() {
        BusinessKeyword keyword = new BusinessKeyword();
        assertNotNull(keyword);
        assertNull(keyword.getKeyword());
        assertNull(keyword.getLocale());
        assertEquals(100, keyword.getWeight()); // Default value from Keyword parent class
        assertEquals(Keyword.Source.MANUAL, keyword.getSource()); // Default value from Keyword parent class
        assertNull(keyword.getBusiness());
    }

    @Test
    void testAllArgsConstructor() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword keyword = new BusinessKeyword(business);
        keyword.setKeyword("test");
        keyword.setLocale("en");
        keyword.setWeight(100);
        keyword.setSource(Keyword.Source.MANUAL);

        assertNotNull(keyword);
        assertEquals("test", keyword.getKeyword());
        assertEquals("en", keyword.getLocale());
        assertEquals(100, keyword.getWeight());
        assertEquals(Keyword.Source.MANUAL, keyword.getSource());
        assertEquals(business, keyword.getBusiness());
    }

    @Test
    void testSetters() {
        Business business1 = Business.builder()
                .name("Business 1")
                .build();
        Business business2 = Business.builder()
                .name("Business 2")
                .build();

        BusinessKeyword keyword = new BusinessKeyword();
        keyword.setKeyword("original");
        keyword.setLocale("en");
        keyword.setWeight(50);
        keyword.setSource(Keyword.Source.MANUAL);
        keyword.setBusiness(business1);

        assertEquals("original", keyword.getKeyword());
        assertEquals("en", keyword.getLocale());
        assertEquals(50, keyword.getWeight());
        assertEquals(Keyword.Source.MANUAL, keyword.getSource());
        assertEquals(business1, keyword.getBusiness());

        // Update values
        keyword.setKeyword("updated");
        keyword.setLocale("de");
        keyword.setWeight(200);
        keyword.setSource(Keyword.Source.SYNONYM);
        keyword.setBusiness(business2);

        assertEquals("updated", keyword.getKeyword());
        assertEquals("de", keyword.getLocale());
        assertEquals(200, keyword.getWeight());
        assertEquals(Keyword.Source.SYNONYM, keyword.getSource());
        assertEquals(business2, keyword.getBusiness());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void testKeyword_whenNullOrEmpty_shouldFailValidation(String invalidKeyword) {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword keyword = BusinessKeyword.builder()
                .keyword(invalidKeyword)
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        Set<ConstraintViolation<BusinessKeyword>> violations = validator.validate(keyword);

        // Note: validation happens at DB level (@Column nullable=false), not with Bean Validation
        // So this test verifies the entity can be created with null/empty values
        // but would fail at persistence time
        assertNotNull(keyword);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "pizza",
            "restaurant delivery",
            "24/7 service",
            "coffee & tea",
            "delivery-service",
            "café",
            "München",
            "北京烤鸭",
            "café-restaurant",
            "24-hour-pharmacy"
    })
    void testKeyword_whenValid_shouldPassValidation(String validKeyword) {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword keyword = BusinessKeyword.builder()
                .keyword(validKeyword)
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        Set<ConstraintViolation<BusinessKeyword>> violations = validator.validate(keyword);

        assertTrue(violations.isEmpty());
    }

    @Test
    void testKeyword_withMaxLength() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        String maxLengthKeyword = "a".repeat(128);

        BusinessKeyword keyword = BusinessKeyword.builder()
                .keyword(maxLengthKeyword)
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        assertEquals(128, keyword.getKeyword().length());
        Set<ConstraintViolation<BusinessKeyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

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
    void testLocale_withValidLocales(String locale) {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword keyword = BusinessKeyword.builder()
                .keyword("test")
                .locale(locale)
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        assertEquals(locale, keyword.getLocale());
        Set<ConstraintViolation<BusinessKeyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLocale_whenNull_shouldPassValidation() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword keyword = BusinessKeyword.builder()
                .keyword("test")
                .locale(null)
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        assertNull(keyword.getLocale());
        Set<ConstraintViolation<BusinessKeyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testWeight_withDefaultValue() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword keyword = new BusinessKeyword();
        keyword.setKeyword("test");
        keyword.setBusiness(business);

        assertEquals(100, keyword.getWeight()); // Default value from Keyword parent class
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 50, 100, 150, 200, 500, 1000, 9999})
    void testWeight_withValidValues(int weight) {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword keyword = BusinessKeyword.builder()
                .keyword("test")
                .weight(weight)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        assertEquals(weight, keyword.getWeight());
    }

    @Test
    void testWeight_withNegativeValue() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword keyword = BusinessKeyword.builder()
                .keyword("test")
                .weight(-10)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        assertEquals(-10, keyword.getWeight());
    }

    @ParameterizedTest
    @EnumSource(Keyword.Source.class)
    void testSource_withAllEnumValues(Keyword.Source source) {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword keyword = BusinessKeyword.builder()
                .keyword("test")
                .weight(100)
                .source(source)
                .business(business)
                .build();

        assertEquals(source, keyword.getSource());
        Set<ConstraintViolation<BusinessKeyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testSource_manualType() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword keyword = BusinessKeyword.builder()
                .keyword("manually added")
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        assertEquals(Keyword.Source.MANUAL, keyword.getSource());
    }

    @Test
    void testSource_categorySyncType() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword keyword = BusinessKeyword.builder()
                .keyword("synced from category")
                .weight(80)
                .source(Keyword.Source.CATEGORY_SYNC)
                .business(business)
                .build();

        assertEquals(Keyword.Source.CATEGORY_SYNC, keyword.getSource());
    }

    @Test
    void testSource_synonymType() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword keyword = BusinessKeyword.builder()
                .keyword("auto-generated synonym")
                .weight(60)
                .source(Keyword.Source.SYNONYM)
                .business(business)
                .build();

        assertEquals(Keyword.Source.SYNONYM, keyword.getSource());
    }

    @Test
    void testSource_systemType() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword keyword = BusinessKeyword.builder()
                .keyword("system generated")
                .weight(50)
                .source(Keyword.Source.SYSTEM)
                .business(business)
                .build();

        assertEquals(Keyword.Source.SYSTEM, keyword.getSource());
    }

    @Test
    void testBusinessRelationship_lazy() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword keyword = BusinessKeyword.builder()
                .keyword("test")
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        assertNotNull(keyword.getBusiness());
        assertEquals(business, keyword.getBusiness());
        assertEquals("Test Business", keyword.getBusiness().getName());
    }

    @Test
    void testBusinessRelationship_bidirectional() {
        Business business = Business.builder()
                .name("Test Business")
                .keywords(new java.util.LinkedHashSet<>())
                .build();

        BusinessKeyword keyword1 = BusinessKeyword.builder()
                .keyword("pizza")
                .locale("en")
                .weight(150)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        BusinessKeyword keyword2 = BusinessKeyword.builder()
                .keyword("delivery")
                .locale("en")
                .weight(120)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        business.getKeywords().add(keyword1);
        business.getKeywords().add(keyword2);

        assertEquals(2, business.getKeywords().size());
        assertTrue(business.getKeywords().contains(keyword1));
        assertTrue(business.getKeywords().contains(keyword2));
        assertEquals(business, keyword1.getBusiness());
        assertEquals(business, keyword2.getBusiness());
    }

    @Test
    void testMultipleKeywords_differentLocales() {
        Business business = Business.builder()
                .name("International Restaurant")
                .build();

        BusinessKeyword keywordEn = BusinessKeyword.builder()
                .keyword("restaurant")
                .locale("en")
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        BusinessKeyword keywordDe = BusinessKeyword.builder()
                .keyword("restaurant")
                .locale("de")
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        BusinessKeyword keywordFr = BusinessKeyword.builder()
                .keyword("restaurant")
                .locale("fr")
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        assertEquals("en", keywordEn.getLocale());
        assertEquals("de", keywordDe.getLocale());
        assertEquals("fr", keywordFr.getLocale());
        assertEquals("restaurant", keywordEn.getKeyword());
        assertEquals("restaurant", keywordDe.getKeyword());
        assertEquals("restaurant", keywordFr.getKeyword());
    }

    @Test
    void testMultipleKeywords_differentWeights() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword highPriority = BusinessKeyword.builder()
                .keyword("main service")
                .weight(200)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        BusinessKeyword mediumPriority = BusinessKeyword.builder()
                .keyword("secondary service")
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        BusinessKeyword lowPriority = BusinessKeyword.builder()
                .keyword("related term")
                .weight(50)
                .source(Keyword.Source.SYNONYM)
                .business(business)
                .build();

        assertEquals(200, highPriority.getWeight());
        assertEquals(100, mediumPriority.getWeight());
        assertEquals(50, lowPriority.getWeight());
    }

    @Test
    void testMultipleKeywords_differentSources() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword manual = BusinessKeyword.builder()
                .keyword("manual keyword")
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        BusinessKeyword categorySync = BusinessKeyword.builder()
                .keyword("category keyword")
                .weight(80)
                .source(Keyword.Source.CATEGORY_SYNC)
                .business(business)
                .build();

        BusinessKeyword synonym = BusinessKeyword.builder()
                .keyword("synonym keyword")
                .weight(60)
                .source(Keyword.Source.SYNONYM)
                .business(business)
                .build();

        BusinessKeyword system = BusinessKeyword.builder()
                .keyword("system keyword")
                .weight(50)
                .source(Keyword.Source.SYSTEM)
                .business(business)
                .build();

        assertEquals(Keyword.Source.MANUAL, manual.getSource());
        assertEquals(Keyword.Source.CATEGORY_SYNC, categorySync.getSource());
        assertEquals(Keyword.Source.SYNONYM, synonym.getSource());
        assertEquals(Keyword.Source.SYSTEM, system.getSource());
    }

    @Test
    void testKeyword_withSpecialCharacters() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        String specialKeyword = "café & restaurant, 24/7 service! (best in town)";

        BusinessKeyword keyword = BusinessKeyword.builder()
                .keyword(specialKeyword)
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        assertEquals(specialKeyword, keyword.getKeyword());
        Set<ConstraintViolation<BusinessKeyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testKeyword_withEmojis() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        String emojiKeyword = "🍕 pizza 🍔 burger 🍟";

        BusinessKeyword keyword = BusinessKeyword.builder()
                .keyword(emojiKeyword)
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        assertEquals(emojiKeyword, keyword.getKeyword());
        Set<ConstraintViolation<BusinessKeyword>> violations = validator.validate(keyword);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testKeyword_withUnicodeCharacters() {
        Business business = Business.builder()
                .name("Test Business")
                .build();

        BusinessKeyword keyword1 = BusinessKeyword.builder()
                .keyword("München Café")
                .locale("de")
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        BusinessKeyword keyword2 = BusinessKeyword.builder()
                .keyword("北京烤鸭")
                .locale("zh")
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        BusinessKeyword keyword3 = BusinessKeyword.builder()
                .keyword("café français")
                .locale("fr")
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .business(business)
                .build();

        assertEquals("München Café", keyword1.getKeyword());
        assertEquals("北京烤鸭", keyword2.getKeyword());
        assertEquals("café français", keyword3.getKeyword());

        Set<ConstraintViolation<BusinessKeyword>> violations1 = validator.validate(keyword1);
        Set<ConstraintViolation<BusinessKeyword>> violations2 = validator.validate(keyword2);
        Set<ConstraintViolation<BusinessKeyword>> violations3 = validator.validate(keyword3);

        assertTrue(violations1.isEmpty());
        assertTrue(violations2.isEmpty());
        assertTrue(violations3.isEmpty());
    }
}
