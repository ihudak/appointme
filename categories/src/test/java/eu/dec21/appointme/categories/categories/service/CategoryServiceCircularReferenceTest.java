package eu.dec21.appointme.categories.categories.service;

import eu.dec21.appointme.categories.categories.entity.Category;
import eu.dec21.appointme.categories.categories.exception.CategoryHierarchyDepthExceededException;
import eu.dec21.appointme.categories.categories.exception.CircularCategoryReferenceException;
import eu.dec21.appointme.categories.categories.mapper.CategoryMapper;
import eu.dec21.appointme.categories.categories.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for circular reference protection in CategoryService.
 * These tests verify that the service properly handles:
 * - Circular references (A→B→A, A→B→C→A)
 * - Maximum depth limits
 * - Deep hierarchies
 * - Branch explosion scenarios
 * - Edge cases in recursive traversal
 * 
 * Note: These tests use ReflectionTestUtils to override maxHierarchyDepth for testing different scenarios.
 * The actual default value is configured in application.yaml and can be overridden via environment variable.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService - Circular Reference Protection Tests")
class CategoryServiceCircularReferenceTest {

    @Mock
    private CategoryMapper categoryMapper;
    
    @Mock
    private CategoryRepository categoryRepository;
    
    @InjectMocks
    private CategoryService categoryService;

    private Category createCategory(Long id, String name, boolean active) {
        return Category.builder()
                .id(id)
                .name(name)
                .active(active)
                .build();
    }

    @Nested
    @DisplayName("1. Circular Reference Detection Tests")
    class CircularReferenceDetectionTests {

        @Test
        @DisplayName("Should detect simple circular reference: A→B→A")
        void shouldDetectSimpleCircularReference() {
            // Arrange
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 5);
            
            Category catA = createCategory(1L, "Category A", true);
            Category catB = createCategory(2L, "Category B", true);
            
            // Setup: A has child B, B has child A (circular!)
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(catA));
            when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of(catB));
            when(categoryRepository.findByParentIdAndActiveTrue(2L)).thenReturn(List.of(catA));  // Circular!

            // Act & Assert
            assertThatThrownBy(() -> categoryService.findAllActiveSubcategoryIdsRecursively(1L))
                    .isInstanceOf(CircularCategoryReferenceException.class)
                    .hasMessageContaining("Circular reference detected")
                    .hasMessageContaining("1");
        }

        @Test
        @DisplayName("Should detect circular reference: A→B→C→A")
        void shouldDetectThreeNodeCircularReference() {
            // Arrange
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 5);
            
            Category catA = createCategory(1L, "A", true);
            Category catB = createCategory(2L, "B", true);
            Category catC = createCategory(3L, "C", true);
            
            // Setup: A→B→C→A (circular with 3 nodes)
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(catA));
            when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of(catB));
            when(categoryRepository.findByParentIdAndActiveTrue(2L)).thenReturn(List.of(catC));
            when(categoryRepository.findByParentIdAndActiveTrue(3L)).thenReturn(List.of(catA));  // Back to A!

            // Act & Assert
            assertThatThrownBy(() -> categoryService.findAllActiveSubcategoryIdsRecursively(1L))
                    .isInstanceOf(CircularCategoryReferenceException.class)
                    .hasMessageContaining("Circular reference detected");
        }

        @Test
        @DisplayName("Should detect circular reference in complex hierarchy: A→B→C→D→B")
        void shouldDetectCircularReferenceInComplexHierarchy() {
            // Arrange
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 10);
            
            Category catA = createCategory(1L, "A", true);
            Category catB = createCategory(2L, "B", true);
            Category catC = createCategory(3L, "C", true);
            Category catD = createCategory(4L, "D", true);
            
            // Setup: A→B→C→D→B (circular back to B, not root)
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(catA));
            when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of(catB));
            when(categoryRepository.findByParentIdAndActiveTrue(2L)).thenReturn(List.of(catC));
            when(categoryRepository.findByParentIdAndActiveTrue(3L)).thenReturn(List.of(catD));
            when(categoryRepository.findByParentIdAndActiveTrue(4L)).thenReturn(List.of(catB));  // Back to B!

            // Act & Assert
            assertThatThrownBy(() -> categoryService.findAllActiveSubcategoryIdsRecursively(1L))
                    .isInstanceOf(CircularCategoryReferenceException.class)
                    .hasMessageContaining("2");  // Category B (id=2) is revisited
        }

        @Test
        @DisplayName("Should detect circular reference with includeInactive=true")
        void shouldDetectCircularReferenceWithInactiveCategories() {
            // Arrange
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 5);
            
            Category catA = createCategory(1L, "A", false);  // Inactive
            Category catB = createCategory(2L, "B", true);
            
            // Setup: A→B→A circular with inactive parent
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(catA));
            when(categoryRepository.findByParentId(1L)).thenReturn(List.of(catB));
            when(categoryRepository.findByParentId(2L)).thenReturn(List.of(catA));

            // Act & Assert
            assertThatThrownBy(() -> categoryService.findAllSubcategoryIdsRecursively(1L, true))
                    .isInstanceOf(CircularCategoryReferenceException.class)
                    .hasMessageContaining("Circular reference detected");
        }

        @Test
        @DisplayName("Should detect self-referencing category: A→A")
        void shouldDetectSelfReferencingCategory() {
            // Arrange
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 5);
            
            Category catA = createCategory(1L, "A", true);
            
            // Setup: A has itself as child (self-reference)
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(catA));
            when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of(catA));

            // Act & Assert
            assertThatThrownBy(() -> categoryService.findAllActiveSubcategoryIdsRecursively(1L))
                    .isInstanceOf(CircularCategoryReferenceException.class)
                    .hasMessageContaining("1");
        }
    }

    @Nested
    @DisplayName("2. Maximum Depth Protection Tests")
    class MaximumDepthProtectionTests {

        @Test
        @DisplayName("Should reject hierarchy exceeding max depth (depth=5, max=5)")
        void shouldRejectHierarchyExceedingMaxDepth() {
            // Arrange
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 5);
            
            // Create 6-level hierarchy: 1→2→3→4→5→6 (exceeds max of 5)
            Category cat1 = createCategory(1L, "Level 1", true);
            Category cat2 = createCategory(2L, "Level 2", true);
            Category cat3 = createCategory(3L, "Level 3", true);
            Category cat4 = createCategory(4L, "Level 4", true);
            Category cat5 = createCategory(5L, "Level 5", true);
            Category cat6 = createCategory(6L, "Level 6", true);
            
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat1));
            when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of(cat2));
            when(categoryRepository.findByParentIdAndActiveTrue(2L)).thenReturn(List.of(cat3));
            when(categoryRepository.findByParentIdAndActiveTrue(3L)).thenReturn(List.of(cat4));
            when(categoryRepository.findByParentIdAndActiveTrue(4L)).thenReturn(List.of(cat5));
            when(categoryRepository.findByParentIdAndActiveTrue(5L)).thenReturn(List.of(cat6));

            // Act & Assert
            assertThatThrownBy(() -> categoryService.findAllActiveSubcategoryIdsRecursively(1L))
                    .isInstanceOf(CategoryHierarchyDepthExceededException.class)
                    .hasMessageContaining("hierarchy depth")
                    .hasMessageContaining("5");
        }

        @Test
        @DisplayName("Should allow hierarchy exactly at max depth (depth=5, max=5)")
        void shouldAllowHierarchyExactlyAtMaxDepth() {
            // Arrange
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 5);
            
            // Create 5-level hierarchy: 1→2→3→4→5 (exactly at max)
            Category cat1 = createCategory(1L, "Level 1", true);
            Category cat2 = createCategory(2L, "Level 2", true);
            Category cat3 = createCategory(3L, "Level 3", true);
            Category cat4 = createCategory(4L, "Level 4", true);
            Category cat5 = createCategory(5L, "Level 5", true);
            
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat1));
            when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of(cat2));
            when(categoryRepository.findByParentIdAndActiveTrue(2L)).thenReturn(List.of(cat3));
            when(categoryRepository.findByParentIdAndActiveTrue(3L)).thenReturn(List.of(cat4));
            when(categoryRepository.findByParentIdAndActiveTrue(4L)).thenReturn(List.of(cat5));
            when(categoryRepository.findByParentIdAndActiveTrue(5L)).thenReturn(List.of());  // No more children

            // Act
            Set<Long> result = categoryService.findAllActiveSubcategoryIdsRecursively(1L);

            // Assert - Should succeed and collect all IDs
            assertThat(result).containsExactlyInAnyOrder(2L, 3L, 4L, 5L);
        }

        @Test
        @DisplayName("Should reject very deep hierarchy (depth=10, max=5)")
        void shouldRejectVeryDeepHierarchy() {
            // Arrange
            int maxDepth = 5;
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", maxDepth);
            
            // Only stub findById for root category
            Category root = createCategory(1L, "Level 1", true);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));
            
            // Create hierarchy only up to maxDepth (exception thrown when trying to go deeper)
            // Depth 0: ID 1 (root)
            // Depth 1: ID 2 (child of 1)
            // Depth 2: ID 3 (child of 2)
            // Depth 3: ID 4 (child of 3)
            // Depth 4: ID 5 (child of 4)
            // Depth 5: Exception thrown when trying to process ID 6
            for (long i = 1; i <= maxDepth; i++) {
                Category child = createCategory(i + 1, "Level " + (i + 1), true);
                when(categoryRepository.findByParentIdAndActiveTrue(i)).thenReturn(List.of(child));
            }

            // Act & Assert
            assertThatThrownBy(() -> categoryService.findAllActiveSubcategoryIdsRecursively(1L))
                    .isInstanceOf(CategoryHierarchyDepthExceededException.class)
                    .hasMessageContaining("exceeded maximum allowed depth of " + maxDepth);
        }

        @Test
        @DisplayName("Should respect custom max depth from configuration")
        void shouldRespectCustomMaxDepth() {
            // Arrange - Set custom max depth of 10
            int customMaxDepth = 10;
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", customMaxDepth);
            
            // Only stub findById for root category
            Category root = createCategory(1L, "Level 1", true);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));
            
            // Create hierarchy only up to customMaxDepth (exception thrown when trying to go deeper)
            for (long i = 1; i <= customMaxDepth; i++) {
                Category child = createCategory(i + 1, "Level " + (i + 1), true);
                when(categoryRepository.findByParentIdAndActiveTrue(i)).thenReturn(List.of(child));
            }

            // Act & Assert
            assertThatThrownBy(() -> categoryService.findAllActiveSubcategoryIdsRecursively(1L))
                    .isInstanceOf(CategoryHierarchyDepthExceededException.class)
                    .hasMessageContaining("exceeded maximum allowed depth of " + customMaxDepth);
        }
    }

    @Nested
    @DisplayName("3. Branch Explosion Scenarios")
    class BranchExplosionTests {

        @Test
        @DisplayName("Should handle wide tree (each node has many children)")
        void shouldHandleWideTree() {
            // Arrange
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 3);
            
            Category root = createCategory(1L, "Root", true);
            
            // Level 1: 10 children
            Category[] level1 = new Category[10];
            for (int i = 0; i < 10; i++) {
                level1[i] = createCategory((long) (2 + i), "L1-" + i, true);
            }
            
            // Level 2: Each level1 category has 5 children (50 total)
            int idCounter = 12;
            for (int i = 0; i < 10; i++) {
                Category[] level2 = new Category[5];
                for (int j = 0; j < 5; j++) {
                    level2[j] = createCategory((long) idCounter++, "L2-" + i + "-" + j, true);
                }
                when(categoryRepository.findByParentIdAndActiveTrue(level1[i].getId()))
                        .thenReturn(List.of(level2));
            }
            
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));
            when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of(level1));
            
            // Mock empty children for level 2
            for (long i = 12; i < 62; i++) {
                when(categoryRepository.findByParentIdAndActiveTrue(i)).thenReturn(List.of());
            }

            // Act
            Set<Long> result = categoryService.findAllActiveSubcategoryIdsRecursively(1L);

            // Assert - Should collect all 60 IDs (10 level1 + 50 level2)
            assertThat(result).hasSize(60);
        }

        @Test
        @DisplayName("Should handle complex branching without circular refs")
        void shouldHandleComplexBranchingWithoutCircularRefs() {
            // Arrange
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 4);
            
            // Root → [A, B]
            // A → [C, D]
            // B → [E]
            // C → [F]
            // D, E, F → [] (no children)
            
            Category root = createCategory(1L, "Root", true);
            Category catA = createCategory(2L, "A", true);
            Category catB = createCategory(3L, "B", true);
            Category catC = createCategory(4L, "C", true);
            Category catD = createCategory(5L, "D", true);
            Category catE = createCategory(6L, "E", true);
            Category catF = createCategory(7L, "F", true);
            
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));
            when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of(catA, catB));
            when(categoryRepository.findByParentIdAndActiveTrue(2L)).thenReturn(List.of(catC, catD));
            when(categoryRepository.findByParentIdAndActiveTrue(3L)).thenReturn(List.of(catE));
            when(categoryRepository.findByParentIdAndActiveTrue(4L)).thenReturn(List.of(catF));
            when(categoryRepository.findByParentIdAndActiveTrue(5L)).thenReturn(List.of());
            when(categoryRepository.findByParentIdAndActiveTrue(6L)).thenReturn(List.of());
            when(categoryRepository.findByParentIdAndActiveTrue(7L)).thenReturn(List.of());

            // Act
            Set<Long> result = categoryService.findAllActiveSubcategoryIdsRecursively(1L);

            // Assert
            assertThat(result).containsExactlyInAnyOrder(2L, 3L, 4L, 5L, 6L, 7L);
        }
    }

    @Nested
    @DisplayName("4. Edge Cases and Normal Operation")
    class EdgeCasesAndNormalOperationTests {

        @Test
        @DisplayName("Should handle category with no children")
        void shouldHandleCategoryWithNoChildren() {
            // Arrange
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 5);
            
            Category leaf = createCategory(1L, "Leaf", true);
            
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(leaf));
            when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of());

            // Act
            Set<Long> result = categoryService.findAllActiveSubcategoryIdsRecursively(1L);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should handle single level hierarchy successfully")
        void shouldHandleSingleLevelHierarchySuccessfully() {
            // Arrange
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 5);
            
            Category parent = createCategory(1L, "Parent", true);
            Category child1 = createCategory(2L, "Child 1", true);
            Category child2 = createCategory(3L, "Child 2", true);
            
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
            when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of(child1, child2));
            when(categoryRepository.findByParentIdAndActiveTrue(2L)).thenReturn(List.of());
            when(categoryRepository.findByParentIdAndActiveTrue(3L)).thenReturn(List.of());

            // Act
            Set<Long> result = categoryService.findAllActiveSubcategoryIdsRecursively(1L);

            // Assert
            assertThat(result).containsExactlyInAnyOrder(2L, 3L);
        }

        @Test
        @DisplayName("Should handle hierarchy at max depth minus one")
        void shouldHandleHierarchyAtMaxDepthMinusOne() {
            // Arrange
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 5);
            
            // Create 4-level hierarchy (depth 4, max is 5)
            Category cat1 = createCategory(1L, "L1", true);
            Category cat2 = createCategory(2L, "L2", true);
            Category cat3 = createCategory(3L, "L3", true);
            Category cat4 = createCategory(4L, "L4", true);
            
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(cat1));
            when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of(cat2));
            when(categoryRepository.findByParentIdAndActiveTrue(2L)).thenReturn(List.of(cat3));
            when(categoryRepository.findByParentIdAndActiveTrue(3L)).thenReturn(List.of(cat4));
            when(categoryRepository.findByParentIdAndActiveTrue(4L)).thenReturn(List.of());

            // Act
            Set<Long> result = categoryService.findAllActiveSubcategoryIdsRecursively(1L);

            // Assert - Should succeed
            assertThat(result).containsExactlyInAnyOrder(2L, 3L, 4L);
        }

        @Test
        @DisplayName("Should not add visited parent to result set")
        void shouldNotAddVisitedParentToResultSet() {
            // Arrange
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 3);
            
            Category parent = createCategory(1L, "Parent", true);
            Category child = createCategory(2L, "Child", true);
            
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(parent));
            when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of(child));
            when(categoryRepository.findByParentIdAndActiveTrue(2L)).thenReturn(List.of());

            // Act
            Set<Long> result = categoryService.findAllActiveSubcategoryIdsRecursively(1L);

            // Assert - Parent ID (1) should NOT be in result, only child (2)
            assertThat(result)
                    .containsExactly(2L)
                    .doesNotContain(1L);
        }
    }

    @Nested
    @DisplayName("5. Performance and Stress Tests")
    class PerformanceAndStressTests {

        @Test
        @DisplayName("Should handle maximum depth hierarchy efficiently")
        void shouldHandleMaximumDepthHierarchyEfficiently() {
            // Arrange
            int maxDepth = 5;
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", maxDepth);
            
            // Only stub findById for root category
            Category root = createCategory(1L, "Level 1", true);
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));
            
            // Create exactly maxDepth-level deep hierarchy (at boundary)
            for (long i = 1; i < maxDepth; i++) {
                Category child = createCategory(i + 1, "Level " + (i + 1), true);
                when(categoryRepository.findByParentIdAndActiveTrue(i)).thenReturn(List.of(child));
            }
            // Last level has no children
            when(categoryRepository.findByParentIdAndActiveTrue((long) maxDepth)).thenReturn(List.of());

            // Act
            long startTime = System.currentTimeMillis();
            Set<Long> result = categoryService.findAllActiveSubcategoryIdsRecursively(1L);
            long duration = System.currentTimeMillis() - startTime;

            // Assert
            assertThat(result).containsExactlyInAnyOrder(2L, 3L, 4L, 5L);
            assertThat(duration).isLessThan(1000); // Should complete in under 1 second
        }

        @Test
        @DisplayName("Should handle moderate branching without performance issues")
        void shouldHandleModerateBranchingWithoutPerformanceIssues() {
            // Arrange
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 3);
            
            Category root = createCategory(1L, "Root", true);
            
            // Create 3 children at level 1
            Category[] level1 = new Category[3];
            for (int i = 0; i < 3; i++) {
                level1[i] = createCategory((long) (2 + i), "L1-" + i, true);
            }
            
            // Each level1 has 3 children (9 total at level 2)
            int idCounter = 5;
            for (int i = 0; i < 3; i++) {
                Category[] level2 = new Category[3];
                for (int j = 0; j < 3; j++) {
                    level2[j] = createCategory((long) idCounter++, "L2-" + i + "-" + j, true);
                }
                when(categoryRepository.findByParentIdAndActiveTrue(level1[i].getId()))
                        .thenReturn(List.of(level2));
            }
            
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));
            when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of(level1));
            
            // Mock empty children for level 2
            for (long i = 5; i < 14; i++) {
                when(categoryRepository.findByParentIdAndActiveTrue(i)).thenReturn(List.of());
            }

            // Act
            long startTime = System.currentTimeMillis();
            Set<Long> result = categoryService.findAllActiveSubcategoryIdsRecursively(1L);
            long duration = System.currentTimeMillis() - startTime;

            // Assert
            assertThat(result).hasSize(12); // 3 at level1 + 9 at level2
            assertThat(duration).isLessThan(1000);
        }
    }
}
