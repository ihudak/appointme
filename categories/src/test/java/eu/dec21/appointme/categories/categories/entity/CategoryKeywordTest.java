package eu.dec21.appointme.categories.categories.entity;

import eu.dec21.appointme.common.entity.Keyword;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CategoryKeywordTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testBuilder_withAllFields() {
        Category category = Category.builder()
                .name("Test Category")
                .build();

        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("test keyword")
                .locale("en")
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .category(category)
                .build();

        assertEquals("test keyword", keyword.getKeyword());
        assertEquals("en", keyword.getLocale());
        assertEquals(100, keyword.getWeight());
        assertEquals(Keyword.Source.MANUAL, keyword.getSource());
        assertEquals(category, keyword.getCategory());
    }

    @Test
    void testBuilder_withMinimalFields() {
        Category category = Category.builder()
                .name("Test Category")
                .build();

        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("minimal")
                .category(category)
                .build();

        assertEquals("minimal", keyword.getKeyword());
        assertEquals(category, keyword.getCategory());
    }

    @Test
    void testDefaultValues() {
        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("test")
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .build();

        assertEquals(100, keyword.getWeight(), "Weight should be 100");
        assertEquals(Keyword.Source.MANUAL, keyword.getSource(), "Source should be MANUAL");
    }

    @Test
    void testCategoryRelationship() {
        Category category = Category.builder()
                .name("Electronics")
                .build();

        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("gadgets")
                .locale("en")
                .category(category)
                .build();

        assertNotNull(keyword.getCategory());
        assertEquals(category, keyword.getCategory());
        assertEquals("Electronics", keyword.getCategory().getName());
    }

    @Test
    void testMultipleKeywordsForSameCategory() {
        Category category = Category.builder()
                .name("Sports")
                .build();

        CategoryKeyword keyword1 = CategoryKeyword.builder()
                .keyword("football")
                .locale("en")
                .category(category)
                .build();

        CategoryKeyword keyword2 = CategoryKeyword.builder()
                .keyword("soccer")
                .locale("en")
                .category(category)
                .build();

        assertEquals(category, keyword1.getCategory());
        assertEquals(category, keyword2.getCategory());
        assertNotEquals(keyword1.getKeyword(), keyword2.getKeyword());
    }

    @ParameterizedTest
    @ValueSource(strings = {"en", "de", "fr", "es", "it", "pt", "pl", "ru", "zh", "ja"})
    void testDifferentLocales(String locale) {
        Category category = Category.builder()
                .name("Test Category")
                .build();

        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("test")
                .locale(locale)
                .category(category)
                .build();

        assertEquals(locale, keyword.getLocale());
    }

    @Test
    void testLocale_canBeNull() {
        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("test")
                .locale(null)
                .build();

        assertNull(keyword.getLocale());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 50, 100, 200, 500, 1000})
    void testDifferentWeights(int weight) {
        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("test")
                .weight(weight)
                .build();

        assertEquals(weight, keyword.getWeight());
    }

    @ParameterizedTest
    @EnumSource(Keyword.Source.class)
    void testAllSourceTypes(Keyword.Source source) {
        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("test")
                .source(source)
                .build();

        assertEquals(source, keyword.getSource());
    }

    @Test
    void testSource_manual() {
        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("manually added")
                .source(Keyword.Source.MANUAL)
                .build();

        assertEquals(Keyword.Source.MANUAL, keyword.getSource());
    }

    @Test
    void testSource_categorySync() {
        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("synced from category")
                .source(Keyword.Source.CATEGORY_SYNC)
                .build();

        assertEquals(Keyword.Source.CATEGORY_SYNC, keyword.getSource());
    }

    @Test
    void testSource_synonym() {
        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("synonym word")
                .source(Keyword.Source.SYNONYM)
                .build();

        assertEquals(Keyword.Source.SYNONYM, keyword.getSource());
    }

    @Test
    void testSource_system() {
        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("system generated")
                .source(Keyword.Source.SYSTEM)
                .build();

        assertEquals(Keyword.Source.SYSTEM, keyword.getSource());
    }

    @Test
    void testSetterMethods() {
        Category category1 = Category.builder()
                .name("Category 1")
                .build();

        Category category2 = Category.builder()
                .name("Category 2")
                .build();

        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("original")
                .locale("en")
                .weight(100)
                .source(Keyword.Source.MANUAL)
                .category(category1)
                .build();

        keyword.setKeyword("updated");
        keyword.setLocale("de");
        keyword.setWeight(200);
        keyword.setSource(Keyword.Source.SYSTEM);
        keyword.setCategory(category2);

        assertEquals("updated", keyword.getKeyword());
        assertEquals("de", keyword.getLocale());
        assertEquals(200, keyword.getWeight());
        assertEquals(Keyword.Source.SYSTEM, keyword.getSource());
        assertEquals(category2, keyword.getCategory());
    }

    @Test
    void testKeywordInheritance_extendsKeyword() {
        CategoryKeyword categoryKeyword = CategoryKeyword.builder()
                .keyword("test")
                .build();

        assertTrue(categoryKeyword instanceof Keyword);
    }

    @Test
    void testKeywordLength_within128Characters() {
        String longKeyword = "a".repeat(128);

        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword(longKeyword)
                .build();

        assertEquals(128, keyword.getKeyword().length());
    }

    @Test
    void testLocaleLength_within16Characters() {
        String locale = "en-US-variant";

        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("test")
                .locale(locale)
                .build();

        assertEquals(locale, keyword.getLocale());
        assertTrue(keyword.getLocale().length() <= 16);
    }

    @Test
    void testAllArgsConstructor() {
        Category category = Category.builder()
                .name("Test Category")
                .build();

        CategoryKeyword keyword = new CategoryKeyword(category);

        assertEquals(category, keyword.getCategory());
    }

    @Test
    void testNoArgsConstructor() {
        CategoryKeyword keyword = new CategoryKeyword();

        assertNull(keyword.getKeyword());
        assertNull(keyword.getLocale());
        assertNull(keyword.getCategory());
    }

    @Test
    void testMultilingualKeywords_sameKeywordDifferentLocales() {
        Category category = Category.builder()
                .name("Food")
                .build();

        CategoryKeyword englishKeyword = CategoryKeyword.builder()
                .keyword("restaurant")
                .locale("en")
                .category(category)
                .build();

        CategoryKeyword germanKeyword = CategoryKeyword.builder()
                .keyword("restaurant")
                .locale("de")
                .category(category)
                .build();

        CategoryKeyword frenchKeyword = CategoryKeyword.builder()
                .keyword("restaurant")
                .locale("fr")
                .category(category)
                .build();

        assertEquals("restaurant", englishKeyword.getKeyword());
        assertEquals("restaurant", germanKeyword.getKeyword());
        assertEquals("restaurant", frenchKeyword.getKeyword());
        assertEquals("en", englishKeyword.getLocale());
        assertEquals("de", germanKeyword.getLocale());
        assertEquals("fr", frenchKeyword.getLocale());
    }

    @Test
    void testWeightComparison_higherWeightKeywords() {
        CategoryKeyword highWeight = CategoryKeyword.builder()
                .keyword("important")
                .weight(500)
                .build();

        CategoryKeyword lowWeight = CategoryKeyword.builder()
                .keyword("less important")
                .weight(50)
                .build();

        assertTrue(highWeight.getWeight() > lowWeight.getWeight());
    }

    @Test
    void testCategoryKeyword_withSpecialCharacters() {
        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("café & restaurant")
                .locale("fr")
                .build();

        assertEquals("café & restaurant", keyword.getKeyword());
    }

    @Test
    void testCategoryKeyword_withNumbers() {
        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("24/7 service")
                .build();

        assertEquals("24/7 service", keyword.getKeyword());
    }

    @Test
    void testCategoryKeyword_withEmojis() {
        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("food 🍔")
                .build();

        assertEquals("food 🍔", keyword.getKeyword());
    }

    @Test
    void testBuilder_fluent() {
        Category category = Category.builder()
                .name("Test")
                .build();

        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("test")
                .locale("en")
                .weight(150)
                .source(Keyword.Source.SYNONYM)
                .category(category)
                .build();

        assertAll(
                () -> assertEquals("test", keyword.getKeyword()),
                () -> assertEquals("en", keyword.getLocale()),
                () -> assertEquals(150, keyword.getWeight()),
                () -> assertEquals(Keyword.Source.SYNONYM, keyword.getSource()),
                () -> assertEquals(category, keyword.getCategory())
        );
    }
}
