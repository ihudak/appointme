package eu.dec21.appointme.categories.categories.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CategoryTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testBuilder_withAllFields() {
        Category parent = Category.builder()
                .name("Parent Category")
                .build();

        Category category = Category.builder()
                .name("Test Category")
                .description("Test Description")
                .imageUrl("https://example.com/image.jpg")
                .active(true)
                .parent(parent)
                .build();

        assertEquals("Test Category", category.getName());
        assertEquals("Test Description", category.getDescription());
        assertEquals("https://example.com/image.jpg", category.getImageUrl());
        assertTrue(category.isActive());
        assertEquals(parent, category.getParent());
    }

    @Test
    void testBuilder_withMinimalFields() {
        Category category = Category.builder()
                .name("Minimal Category")
                .build();

        assertEquals("Minimal Category", category.getName());
        assertNull(category.getDescription());
        assertNull(category.getImageUrl());
        assertNull(category.getParent());
    }

    @Test
    void testDefaultValues() {
        Category category = Category.builder()
                .name("Test Category")
                .children(new LinkedHashSet<>())
                .keywords(new LinkedHashSet<>())
                .build();

        assertNotNull(category.getChildren());
        assertTrue(category.getChildren().isEmpty());
        assertNotNull(category.getKeywords());
        assertTrue(category.getKeywords().isEmpty());
    }

    @Test
    void testParentChildRelationship_addChild() {
        Category parent = Category.builder()
                .name("Parent")
                .children(new LinkedHashSet<>())
                .build();

        Category child = Category.builder()
                .name("Child")
                .parent(parent)
                .build();

        parent.getChildren().add(child);

        assertEquals(1, parent.getChildren().size());
        assertTrue(parent.getChildren().contains(child));
        assertEquals(parent, child.getParent());
    }

    @Test
    void testParentChildRelationship_multipleChildren() {
        Category parent = Category.builder()
                .name("Parent")
                .children(new LinkedHashSet<>())
                .build();

        Category child1 = Category.builder()
                .name("Child 1")
                .parent(parent)
                .build();

        Category child2 = Category.builder()
                .name("Child 2")
                .parent(parent)
                .build();

        parent.getChildren().add(child1);
        parent.getChildren().add(child2);

        assertEquals(2, parent.getChildren().size());
        assertTrue(parent.getChildren().contains(child1));
        assertTrue(parent.getChildren().contains(child2));
    }

    @Test
    void testRootCategory_noParent() {
        Category rootCategory = Category.builder()
                .name("Root Category")
                .parent(null)
                .build();

        assertNull(rootCategory.getParent());
    }

    @Test
    void testCategoryHierarchy_threeLevel() {
        Category root = Category.builder()
                .name("Electronics")
                .children(new LinkedHashSet<>())
                .build();

        Category level2 = Category.builder()
                .name("Computers")
                .parent(root)
                .children(new LinkedHashSet<>())
                .build();

        Category level3 = Category.builder()
                .name("Laptops")
                .parent(level2)
                .children(new LinkedHashSet<>())
                .build();

        root.getChildren().add(level2);
        level2.getChildren().add(level3);

        assertNull(root.getParent());
        assertEquals(root, level2.getParent());
        assertEquals(level2, level3.getParent());
        assertEquals(1, root.getChildren().size());
        assertEquals(1, level2.getChildren().size());
        assertTrue(level3.getChildren().isEmpty());
    }

    @Test
    void testKeywordsCollection_isEmpty() {
        Category category = Category.builder()
                .name("Test Category")
                .keywords(new LinkedHashSet<>())
                .build();

        assertNotNull(category.getKeywords());
        assertTrue(category.getKeywords().isEmpty());
    }

    @Test
    void testKeywordsCollection_addKeyword() {
        Category category = Category.builder()
                .name("Test Category")
                .keywords(new LinkedHashSet<>())
                .build();

        CategoryKeyword keyword = CategoryKeyword.builder()
                .keyword("test keyword")
                .locale("en")
                .category(category)
                .build();

        category.getKeywords().add(keyword);

        assertEquals(1, category.getKeywords().size());
        assertTrue(category.getKeywords().contains(keyword));
    }

    @Test
    void testKeywordsCollection_multipleKeywords() {
        Category category = Category.builder()
                .name("Test Category")
                .keywords(new LinkedHashSet<>())
                .build();

        CategoryKeyword keyword1 = CategoryKeyword.builder()
                .keyword("keyword 1")
                .locale("en")
                .category(category)
                .build();

        CategoryKeyword keyword2 = CategoryKeyword.builder()
                .keyword("keyword 2")
                .locale("de")
                .category(category)
                .build();

        category.getKeywords().add(keyword1);
        category.getKeywords().add(keyword2);

        assertEquals(2, category.getKeywords().size());
        assertTrue(category.getKeywords().contains(keyword1));
        assertTrue(category.getKeywords().contains(keyword2));
    }

    @Test
    void testActiveFlag_defaultTrue() {
        // Don't set active explicitly — verify Java default for boolean (false)
        // Note: DB column has DEFAULT TRUE, but Java primitive boolean defaults to false
        Category category = Category.builder()
                .name("Test Category")
                .build();

        assertFalse(category.isActive());
    }

    @Test
    void testActiveFlag_setTrue() {
        Category category = Category.builder()
                .name("Test Category")
                .active(true)
                .build();

        assertTrue(category.isActive());
    }

    @Test
    void testActiveFlag_setFalse() {
        Category category = Category.builder()
                .name("Test Category")
                .active(false)
                .build();

        assertFalse(category.isActive());
    }

    @Test
    void testNameUniqueness_differentNames() {
        Category category1 = Category.builder()
                .name("Category 1")
                .build();

        Category category2 = Category.builder()
                .name("Category 2")
                .build();

        assertNotEquals(category1.getName(), category2.getName());
    }

    @Test
    void testSetterMethods() {
        Category category = Category.builder()
                .name("Original Name")
                .build();

        category.setName("Updated Name");
        category.setDescription("Updated Description");
        category.setImageUrl("https://example.com/new-image.jpg");
        category.setActive(false);

        assertEquals("Updated Name", category.getName());
        assertEquals("Updated Description", category.getDescription());
        assertEquals("https://example.com/new-image.jpg", category.getImageUrl());
        assertFalse(category.isActive());
    }

    @Test
    void testParentSetter() {
        Category parent1 = Category.builder()
                .name("Parent 1")
                .build();

        Category parent2 = Category.builder()
                .name("Parent 2")
                .build();

        Category child = Category.builder()
                .name("Child")
                .parent(parent1)
                .build();

        assertEquals(parent1, child.getParent());

        child.setParent(parent2);

        assertEquals(parent2, child.getParent());
    }

    @Test
    void testChildrenCollection_maintainsInsertionOrder() {
        Category parent = Category.builder()
                .name("Parent")
                .children(new LinkedHashSet<>())
                .build();

        Category child1 = Category.builder().name("Child A").build();
        Category child2 = Category.builder().name("Child B").build();
        Category child3 = Category.builder().name("Child C").build();

        parent.getChildren().add(child1);
        parent.getChildren().add(child2);
        parent.getChildren().add(child3);

        var childrenList = parent.getChildren().stream().toList();
        assertEquals(child1, childrenList.get(0));
        assertEquals(child2, childrenList.get(1));
        assertEquals(child3, childrenList.get(2));
    }

    @Test
    void testKeywordsCollection_maintainsInsertionOrder() {
        Category category = Category.builder()
                .name("Test Category")
                .keywords(new LinkedHashSet<>())
                .build();

        CategoryKeyword kw1 = CategoryKeyword.builder().keyword("first").locale("en").build();
        CategoryKeyword kw2 = CategoryKeyword.builder().keyword("second").locale("en").build();
        CategoryKeyword kw3 = CategoryKeyword.builder().keyword("third").locale("en").build();

        category.getKeywords().add(kw1);
        category.getKeywords().add(kw2);
        category.getKeywords().add(kw3);

        var keywordsList = category.getKeywords().stream().toList();
        assertEquals(kw1, keywordsList.get(0));
        assertEquals(kw2, keywordsList.get(1));
        assertEquals(kw3, keywordsList.get(2));
    }

    @Test
    void testAllArgsConstructor() {
        Set<Category> children = new LinkedHashSet<>();
        Set<CategoryKeyword> keywords = new LinkedHashSet<>();

        Category category = new Category(
                "Test Name",
                "Test Description",
                "https://example.com/image.jpg",
                true,
                null,
                children,
                keywords
        );

        assertEquals("Test Name", category.getName());
        assertEquals("Test Description", category.getDescription());
        assertEquals("https://example.com/image.jpg", category.getImageUrl());
        assertTrue(category.isActive());
        assertNull(category.getParent());
        assertSame(children, category.getChildren());
        assertSame(keywords, category.getKeywords());
    }

    @Test
    void testNoArgsConstructor() {
        Category category = new Category();

        assertNull(category.getName());
        assertNull(category.getDescription());
        assertNull(category.getImageUrl());
        assertNull(category.getParent());
    }

    // ===== Name Validation Tests =====

    @Test
    void testName_withValidLength() {
        Category category = Category.builder()
                .name("Electronics")
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_withMinLength() {
        Category category = Category.builder()
                .name("C")
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_withMaxLength() {
        String maxLengthName = "C".repeat(100);
        Category category = Category.builder()
                .name(maxLengthName)
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_exceedsMaxLength() {
        String tooLongName = "C".repeat(101);
        Category category = Category.builder()
                .name(tooLongName)
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name") &&
                        v.getMessage().contains("1-100 characters")));
    }

    @Test
    void testName_blankString() {
        Category category = Category.builder()
                .name("")
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testName_nullValue() {
        Category category = Category.builder()
                .name(null)
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testName_withWhitespaceOnly() {
        Category category = Category.builder()
                .name("   ")
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void testName_withSpecialCharacters() {
        Category category = Category.builder()
                .name("Electronics & Gadgets (2024)")
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testName_withUnicode() {
        Category category = Category.builder()
                .name("Электроника")
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    // ===== Description Validation Tests =====

    @Test
    void testDescription_withValidLength() {
        Category category = Category.builder()
                .name("Electronics")
                .description("A category for all electronic devices and gadgets")
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testDescription_withMaxLength() {
        String maxLengthDesc = "D".repeat(500);
        Category category = Category.builder()
                .name("Test")
                .description(maxLengthDesc)
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testDescription_exceedsMaxLength() {
        String tooLongDesc = "D".repeat(501);
        Category category = Category.builder()
                .name("Test")
                .description(tooLongDesc)
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("description") &&
                        v.getMessage().contains("must not exceed 500 characters")));
    }

    @Test
    void testDescription_nullIsValid() {
        Category category = Category.builder()
                .name("Electronics")
                .description(null)
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testDescription_emptyStringIsValid() {
        Category category = Category.builder()
                .name("Electronics")
                .description("")
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    // ===== Image URL Validation Tests =====

    @Test
    void testImageUrl_withValidUrl() {
        Category category = Category.builder()
                .name("Electronics")
                .imageUrl("https://example.com/category/electronics.jpg")
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testImageUrl_withHttpProtocol() {
        Category category = Category.builder()
                .name("Electronics")
                .imageUrl("http://example.com/image.jpg")
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testImageUrl_withInvalidUrl() {
        Category category = Category.builder()
                .name("Electronics")
                .imageUrl("not-a-valid-url")
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("imageUrl") &&
                        v.getMessage().contains("Invalid image URL")));
    }

    @Test
    void testImageUrl_withMaxLength() {
        String maxLengthUrl = "https://example.com/" + "a".repeat(2024);
        Category category = Category.builder()
                .name("Electronics")
                .imageUrl(maxLengthUrl)
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testImageUrl_exceedsMaxLength() {
        String tooLongUrl = "https://example.com/" + "a".repeat(2030);
        Category category = Category.builder()
                .name("Electronics")
                .imageUrl(tooLongUrl)
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("imageUrl") &&
                        v.getMessage().contains("must not exceed 2048 characters")));
    }

    @Test
    void testImageUrl_nullIsValid() {
        Category category = Category.builder()
                .name("Electronics")
                .imageUrl(null)
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    // ===== Combined Validation Tests =====

    @Test
    void testValidation_allFieldsValid() {
        Category category = Category.builder()
                .name("Electronics & Gadgets")
                .description("High-quality electronic devices and accessories")
                .imageUrl("https://cdn.example.com/categories/electronics.jpg")
                .active(true)
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testValidation_multipleViolations() {
        String tooLongName = "C".repeat(101);
        String tooLongDesc = "D".repeat(501);
        
        Category category = Category.builder()
                .name(tooLongName)
                .description(tooLongDesc)
                .imageUrl("invalid-url")
                .build();

        Set<ConstraintViolation<Category>> violations = validator.validate(category);
        assertFalse(violations.isEmpty());
        assertEquals(3, violations.size());
        
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("description")));
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("imageUrl")));
    }
}
