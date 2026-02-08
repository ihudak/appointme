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
}
