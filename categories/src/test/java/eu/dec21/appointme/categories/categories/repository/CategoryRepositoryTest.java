package eu.dec21.appointme.categories.categories.repository;

import eu.dec21.appointme.categories.categories.entity.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DisplayName("CategoryRepository Tests")
class CategoryRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgis/postgis:17-3.5").asCompatibleSubstituteFor("postgres")
    )
            .withDatabaseName("appme_categories")
            .withUsername("pguser")
            .withPassword("p@ssw0rD!");

    @Autowired
    private CategoryRepository categoryRepository;

    private Category rootActive;
    private Category rootInactive;
    private Category childActive1;
    private Category childActive2;
    private Category childInactive;
    private Category grandchildActive;
    private Category grandchildInactive;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();

        // Root categories (parent = null)
        rootActive = createCategory("Root Active", "Active root category", true, null);
        rootInactive = createCategory("Root Inactive", "Inactive root category", false, null);

        // Child categories under rootActive
        childActive1 = createCategory("Child Active 1", "First active child", true, rootActive);
        childActive2 = createCategory("Child Active 2", "Second active child", true, rootActive);
        childInactive = createCategory("Child Inactive", "Inactive child", false, rootActive);

        // Grandchild categories under childActive1
        grandchildActive = createCategory("Grandchild Active", "Active grandchild", true, childActive1);
        grandchildInactive = createCategory("Grandchild Inactive", "Inactive grandchild", false, childActive1);
    }

    // === findByParentIsNull (admin - see all root categories) ===

    @Test
    @DisplayName("findByParentIsNull should return all root categories")
    void findByParentIsNull_shouldReturnAllRootCategories() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Category> result = categoryRepository.findByParentIsNull(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Category::getName)
                .containsExactlyInAnyOrder("Root Active", "Root Inactive");
    }

    @Test
    @DisplayName("findByParentIsNull should support pagination")
    void findByParentIsNull_shouldSupportPagination() {
        Pageable pageable = PageRequest.of(0, 1);

        Page<Category> result = categoryRepository.findByParentIsNull(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByParentIsNull should return empty when no root categories exist")
    void findByParentIsNull_shouldReturnEmptyWhenNoRootCategories() {
        categoryRepository.deleteAll();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> result = categoryRepository.findByParentIsNull(pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }

    // === findByParentIsNullAndActiveTrue (public - only active root categories) ===

    @Test
    @DisplayName("findByParentIsNullAndActiveTrue should return only active root categories")
    void findByParentIsNullAndActiveTrue_shouldReturnOnlyActiveRootCategories() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Category> result = categoryRepository.findByParentIsNullAndActiveTrue(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName()).isEqualTo("Root Active");
        assertThat(result.getContent().get(0).isActive()).isTrue();
    }

    @Test
    @DisplayName("findByParentIsNullAndActiveTrue should support pagination")
    void findByParentIsNullAndActiveTrue_shouldSupportPagination() {
        // Create additional active root categories
        createCategory("Root Active 2", "Second active root", true, null);
        createCategory("Root Active 3", "Third active root", true, null);

        Pageable pageable = PageRequest.of(0, 2);
        Page<Category> result = categoryRepository.findByParentIsNullAndActiveTrue(pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByParentIsNullAndActiveTrue should return empty when no active root categories")
    void findByParentIsNullAndActiveTrue_shouldReturnEmptyWhenNoActiveRoots() {
        // Set all root categories to inactive
        rootActive.setActive(false);
        categoryRepository.save(rootActive);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> result = categoryRepository.findByParentIsNullAndActiveTrue(pageable);

        assertThat(result.getContent()).isEmpty();
    }

    // === findByParentId(Long, Pageable) (admin - see all children) ===

    @Test
    @DisplayName("findByParentId with pageable should return all children of given parent")
    void findByParentId_withPageable_shouldReturnAllChildren() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Category> result = categoryRepository.findByParentId(rootActive.getId(), pageable);

        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getContent()).extracting(Category::getName)
                .containsExactlyInAnyOrder("Child Active 1", "Child Active 2", "Child Inactive");
    }

    @Test
    @DisplayName("findByParentId with pageable should support pagination")
    void findByParentId_withPageable_shouldSupportPagination() {
        Pageable pageable = PageRequest.of(0, 2);

        Page<Category> result = categoryRepository.findByParentId(rootActive.getId(), pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByParentId with pageable should return empty for non-existent parent")
    void findByParentId_withPageable_shouldReturnEmptyForNonExistentParent() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Category> result = categoryRepository.findByParentId(999L, pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findByParentId with pageable should return empty when parent has no children")
    void findByParentId_withPageable_shouldReturnEmptyWhenNoChildren() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Category> result = categoryRepository.findByParentId(childActive2.getId(), pageable);

        assertThat(result.getContent()).isEmpty();
    }

    // === findByParentId(Long) (admin - list all children) ===

    @Test
    @DisplayName("findByParentId without pageable should return all children as list")
    void findByParentId_withoutPageable_shouldReturnAllChildrenAsList() {
        List<Category> result = categoryRepository.findByParentId(rootActive.getId());

        assertThat(result).hasSize(3);
        assertThat(result).extracting(Category::getName)
                .containsExactlyInAnyOrder("Child Active 1", "Child Active 2", "Child Inactive");
    }

    @Test
    @DisplayName("findByParentId without pageable should return empty list for non-existent parent")
    void findByParentId_withoutPageable_shouldReturnEmptyForNonExistentParent() {
        List<Category> result = categoryRepository.findByParentId(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByParentId without pageable should return empty when parent has no children")
    void findByParentId_withoutPageable_shouldReturnEmptyWhenNoChildren() {
        List<Category> result = categoryRepository.findByParentId(childActive2.getId());

        assertThat(result).isEmpty();
    }

    // === findByParentIdAndActiveTrue(Long, Pageable) (public - only active children) ===

    @Test
    @DisplayName("findByParentIdAndActiveTrue with pageable should return only active children")
    void findByParentIdAndActiveTrue_withPageable_shouldReturnOnlyActiveChildren() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Category> result = categoryRepository.findByParentIdAndActiveTrue(rootActive.getId(), pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(Category::getName)
                .containsExactlyInAnyOrder("Child Active 1", "Child Active 2");
        assertThat(result.getContent()).allMatch(Category::isActive);
    }

    @Test
    @DisplayName("findByParentIdAndActiveTrue with pageable should support pagination")
    void findByParentIdAndActiveTrue_withPageable_shouldSupportPagination() {
        Pageable pageable = PageRequest.of(0, 1);

        Page<Category> result = categoryRepository.findByParentIdAndActiveTrue(rootActive.getId(), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(2);
    }

    @Test
    @DisplayName("findByParentIdAndActiveTrue with pageable should return empty when no active children")
    void findByParentIdAndActiveTrue_withPageable_shouldReturnEmptyWhenNoActiveChildren() {
        // Set all active children to inactive
        childActive1.setActive(false);
        childActive2.setActive(false);
        categoryRepository.saveAll(List.of(childActive1, childActive2));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> result = categoryRepository.findByParentIdAndActiveTrue(rootActive.getId(), pageable);

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findByParentIdAndActiveTrue with pageable should return empty for non-existent parent")
    void findByParentIdAndActiveTrue_withPageable_shouldReturnEmptyForNonExistentParent() {
        Pageable pageable = PageRequest.of(0, 10);

        Page<Category> result = categoryRepository.findByParentIdAndActiveTrue(999L, pageable);

        assertThat(result.getContent()).isEmpty();
    }

    // === findByParentIdAndActiveTrue(Long) (public - list only active children) ===

    @Test
    @DisplayName("findByParentIdAndActiveTrue without pageable should return only active children as list")
    void findByParentIdAndActiveTrue_withoutPageable_shouldReturnOnlyActiveChildrenAsList() {
        List<Category> result = categoryRepository.findByParentIdAndActiveTrue(rootActive.getId());

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Category::getName)
                .containsExactlyInAnyOrder("Child Active 1", "Child Active 2");
        assertThat(result).allMatch(Category::isActive);
    }

    @Test
    @DisplayName("findByParentIdAndActiveTrue without pageable should return empty list when no active children")
    void findByParentIdAndActiveTrue_withoutPageable_shouldReturnEmptyWhenNoActiveChildren() {
        // Set all active children to inactive
        childActive1.setActive(false);
        childActive2.setActive(false);
        categoryRepository.saveAll(List.of(childActive1, childActive2));

        List<Category> result = categoryRepository.findByParentIdAndActiveTrue(rootActive.getId());

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByParentIdAndActiveTrue without pageable should return empty for non-existent parent")
    void findByParentIdAndActiveTrue_withoutPageable_shouldReturnEmptyForNonExistentParent() {
        List<Category> result = categoryRepository.findByParentIdAndActiveTrue(999L);

        assertThat(result).isEmpty();
    }

    // === Edge Cases & Complex Scenarios ===

    @Test
    @DisplayName("Should handle multiple levels of hierarchy correctly")
    void shouldHandleMultipleLevelsOfHierarchy() {
        // Verify grandchildren are not included when querying children
        List<Category> childrenOfRoot = categoryRepository.findByParentId(rootActive.getId());
        assertThat(childrenOfRoot).hasSize(3);
        assertThat(childrenOfRoot).extracting(Category::getName)
                .doesNotContain("Grandchild Active", "Grandchild Inactive");

        // Verify grandchildren query works correctly
        List<Category> grandchildren = categoryRepository.findByParentId(childActive1.getId());
        assertThat(grandchildren).hasSize(2);
        assertThat(grandchildren).extracting(Category::getName)
                .containsExactlyInAnyOrder("Grandchild Active", "Grandchild Inactive");
    }

    @Test
    @DisplayName("Should filter inactive parent's children correctly")
    void shouldFilterInactiveParentChildrenCorrectly() {
        // Create children under inactive root
        Category childOfInactive = createCategory("Child of Inactive", "Child under inactive parent", true, rootInactive);

        // Admin query should return it
        List<Category> adminResult = categoryRepository.findByParentId(rootInactive.getId());
        assertThat(adminResult).hasSize(1);
        assertThat(adminResult.get(0).getName()).isEqualTo("Child of Inactive");

        // Public query should also return it (child itself is active, even if parent is not)
        List<Category> publicResult = categoryRepository.findByParentIdAndActiveTrue(rootInactive.getId());
        assertThat(publicResult).hasSize(1);
        assertThat(publicResult.get(0).getName()).isEqualTo("Child of Inactive");
    }

    @Test
    @DisplayName("Should handle null parent ID gracefully")
    void shouldHandleNullParentIdGracefully() {
        List<Category> result = categoryRepository.findByParentId(null);

        // Spring Data JPA treats null as "parent IS NULL", so it returns root categories
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Category::getName)
                .containsExactlyInAnyOrder("Root Active", "Root Inactive");
    }

    @Test
    @DisplayName("Should return consistent results across paginated and non-paginated queries")
    void shouldReturnConsistentResultsAcrossPaginatedAndNonPaginated() {
        // Non-paginated
        List<Category> listResult = categoryRepository.findByParentIdAndActiveTrue(rootActive.getId());

        // Paginated (get all pages)
        Pageable pageable = PageRequest.of(0, 10);
        Page<Category> pageResult = categoryRepository.findByParentIdAndActiveTrue(rootActive.getId(), pageable);

        assertThat(listResult).hasSize(pageResult.getContent().size());
        assertThat(listResult).extracting(Category::getId)
                .containsExactlyInAnyOrderElementsOf(
                        pageResult.getContent().stream().map(Category::getId).toList()
                );
    }

    @Test
    @DisplayName("Should preserve audit fields when querying")
    void shouldPreserveAuditFieldsWhenQuerying() {
        List<Category> result = categoryRepository.findByParentId(rootActive.getId());

        assertThat(result).isNotEmpty();
        assertThat(result).allSatisfy(category -> {
            assertThat(category.getCreatedAt()).isNotNull();
            assertThat(category.getUpdatedAt()).isNotNull();
            assertThat(category.getCreatedBy()).isNotNull();
            assertThat(category.getUpdatedBy()).isNotNull();
        });
    }

    @Test
    @DisplayName("Should handle page out of bounds gracefully")
    void shouldHandlePageOutOfBoundsGracefully() {
        Pageable pageable = PageRequest.of(10, 10); // Page way beyond available data

        Page<Category> result = categoryRepository.findByParentId(rootActive.getId(), pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(3);
        assertThat(result.getNumber()).isEqualTo(10);
    }

    @Test
    @DisplayName("Should handle small page size correctly")
    void shouldHandleSmallPageSize() {
        Pageable pageable = PageRequest.of(0, 1); // Minimum page size

        Page<Category> result = categoryRepository.findByParentId(rootActive.getId(), pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(3);
    }

    // === Helper Methods ===

    private Category createCategory(String name, String description, boolean active, Category parent) {
        Category category = Category.builder()
                .name(name)
                .description(description)
                .active(active)
                .parent(parent)
                .imageUrl("https://example.com/" + name.toLowerCase().replace(" ", "-") + ".jpg")
                .build();

        setAuditFields(category);
        return categoryRepository.save(category);
    }

    private void setAuditFields(Category category) {
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        category.setCreatedBy(1L);
        category.setUpdatedBy(1L);
    }
}
