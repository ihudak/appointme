package eu.dec21.appointme.categories.categories.service;

import eu.dec21.appointme.categories.categories.entity.Category;
import eu.dec21.appointme.categories.categories.exception.CategoryHierarchyDepthExceededException;
import eu.dec21.appointme.categories.categories.exception.CircularCategoryReferenceException;
import eu.dec21.appointme.categories.categories.mapper.CategoryMapper;
import eu.dec21.appointme.categories.categories.repository.CategoryRepository;
import eu.dec21.appointme.categories.categories.request.CategoryRequest;
import eu.dec21.appointme.categories.categories.response.CategoryResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive tests for category hierarchy depth validation.
 * Tests the proactive validation that prevents users from creating hierarchies
 * deeper than the configured maximum depth.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService - Hierarchy Depth Validation Tests")
class CategoryServiceHierarchyDepthValidationTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category createCategory(Long id, String name, Long parentId) {
        Category.CategoryBuilder builder = Category.builder()
                .id(id)
                .name(name)
                .active(true);
        
        // If parentId is provided, create a minimal parent Category with just the ID
        if (parentId != null) {
            Category parent = Category.builder().id(parentId).build();
            builder.parent(parent);
        }
        
        return builder.build();
    }

    private CategoryRequest createRequest(Long id, String name, Long parentId) {
        return new CategoryRequest(id, name, "Test description", parentId);
    }

    private CategoryResponse createResponse(Long id, String name) {
        return CategoryResponse.builder()
                .id(id)
                .name(name)
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("Root Category Creation")
    class RootCategoryCreation {

        @Test
        @DisplayName("Should allow creating root category (no parent)")
        void testSave_RootCategory_Success() {
            // Given
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 5);
            CategoryRequest request = createRequest(null, "Root", null);
            Category category = createCategory(null, "Root", null);
            Category savedCategory = createCategory(1L, "Root", null);
            CategoryResponse response = createResponse(1L, "Root");

            when(categoryMapper.toCategory(request)).thenReturn(category);
            when(categoryRepository.save(category)).thenReturn(savedCategory);
            when(categoryMapper.toCategoryResponse(savedCategory)).thenReturn(response);

            // When
            CategoryResponse result = categoryService.save(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Root");

            // Verify no depth validation was performed (no parent)
            verify(categoryRepository, never()).findById(any());
            verify(categoryRepository).save(category);
        }
    }

    @Nested
    @DisplayName("Valid Hierarchy Depth Creation")
    class ValidHierarchyDepthCreation {

        @Test
        @DisplayName("Should allow creating category at depth 1 (parent is root)")
        void testSave_Depth1_Success() {
            // Given: maxDepth=5, parent is at depth 0 (root)
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 5);
            
            Category parentRoot = createCategory(1L, "Root", null);
            CategoryRequest request = createRequest(null, "Level1", 1L);
            Category category = createCategory(null, "Level1", 1L);
            Category savedCategory = createCategory(2L, "Level1", 1L);
            CategoryResponse response = createResponse(2L, "Level1");

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(parentRoot));
            when(categoryMapper.toCategory(request)).thenReturn(category);
            when(categoryRepository.save(category)).thenReturn(savedCategory);
            when(categoryMapper.toCategoryResponse(savedCategory)).thenReturn(response);

            // When
            CategoryResponse result = categoryService.save(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(2L);
            verify(categoryRepository).findById(1L);
            verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("Should allow creating category at depth 4 (exactly one level below max)")
        void testSave_OneBeforeMaxDepth_Success() {
            // Given: maxDepth=5, creating at depth 4 (parent at depth 3)
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 5);
            
            // Root → Level1 → Level2 → Level3 (parent at depth 3)
            Category root = createCategory(1L, "Root", null);
            Category level1 = createCategory(2L, "Level1", 1L);
            Category level2 = createCategory(3L, "Level2", 2L);
            Category level3 = createCategory(4L, "Level3", 3L);
            
            CategoryRequest request = createRequest(null, "Level4", 4L);
            Category category = createCategory(null, "Level4", 4L);
            Category savedCategory = createCategory(5L, "Level4", 4L);
            CategoryResponse response = createResponse(5L, "Level4");

            when(categoryRepository.findById(4L)).thenReturn(Optional.of(level3));
            when(categoryRepository.findById(3L)).thenReturn(Optional.of(level2));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(level1));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));
            when(categoryMapper.toCategory(request)).thenReturn(category);
            when(categoryRepository.save(category)).thenReturn(savedCategory);
            when(categoryMapper.toCategoryResponse(savedCategory)).thenReturn(response);

            // When
            CategoryResponse result = categoryService.save(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(5L);
            verify(categoryRepository).save(category);
        }
    }

    @Nested
    @DisplayName("Maximum Depth Violation Prevention")
    class MaximumDepthViolationPrevention {

        @Test
        @DisplayName("Should prevent creating category at max depth")
        void testSave_AtMaxDepth_ThrowsException() {
            // Given: maxDepth=5, parent is at depth 4
            // New category would be at depth 5, which equals maxDepth (not allowed)
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 5);
            
            // Root(0) → L1(1) → L2(2) → L3(3) → L4(4)
            Category root = createCategory(1L, "Root", null);
            Category level1 = createCategory(2L, "Level1", 1L);
            Category level2 = createCategory(3L, "Level2", 2L);
            Category level3 = createCategory(4L, "Level3", 3L);
            Category level4 = createCategory(5L, "Level4", 4L); // Parent at depth 4
            
            CategoryRequest request = createRequest(null, "Level5", 5L); // Would be at depth 5

            when(categoryRepository.findById(5L)).thenReturn(Optional.of(level4));
            when(categoryRepository.findById(4L)).thenReturn(Optional.of(level3));
            when(categoryRepository.findById(3L)).thenReturn(Optional.of(level2));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(level1));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));

            // When & Then
            assertThatThrownBy(() -> categoryService.save(request))
                    .isInstanceOf(CategoryHierarchyDepthExceededException.class)
                    .hasMessageContaining("hierarchy depth (5) exceeded maximum allowed depth of 5");

            // Verify category was NOT saved
            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should prevent creating category beyond max depth")
        void testSave_BeyondMaxDepth_ThrowsException() {
            // Given: maxDepth=3, parent is at depth 2
            // New category would be at depth 3, which equals maxDepth (not allowed)
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 3);
            
            Category root = createCategory(1L, "Root", null);
            Category level1 = createCategory(2L, "Level1", 1L);
            Category level2 = createCategory(3L, "Level2", 2L); // Parent at depth 2
            
            CategoryRequest request = createRequest(null, "Level3", 3L); // Would be at depth 3

            when(categoryRepository.findById(3L)).thenReturn(Optional.of(level2));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(level1));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));

            // When & Then
            assertThatThrownBy(() -> categoryService.save(request))
                    .isInstanceOf(CategoryHierarchyDepthExceededException.class)
                    .hasMessageContaining("hierarchy depth (3) exceeded maximum allowed depth of 3");

            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should prevent creating very deep hierarchy with small max depth")
        void testSave_VeryDeepHierarchy_SmallMaxDepth_ThrowsException() {
            // Given: maxDepth=2, parent is at depth 1
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 2);
            
            Category root = createCategory(1L, "Root", null);
            Category level1 = createCategory(2L, "Level1", 1L); // Depth 1
            
            CategoryRequest request = createRequest(null, "Level2", 2L); // Would be at depth 2

            when(categoryRepository.findById(2L)).thenReturn(Optional.of(level1));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));

            // When & Then
            assertThatThrownBy(() -> categoryService.save(request))
                    .isInstanceOf(CategoryHierarchyDepthExceededException.class)
                    .hasMessageContaining("hierarchy depth (2) exceeded maximum allowed depth of 2");

            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Edge Cases and Error Handling")
    class EdgeCasesAndErrorHandling {

        @Test
        @DisplayName("Should throw EntityNotFoundException when parent doesn't exist")
        void testSave_ParentNotFound_ThrowsException() {
            // Given
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 5);
            CategoryRequest request = createRequest(null, "Child", 999L);

            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> categoryService.save(request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Parent category not found with id 999");

            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should detect circular reference during depth calculation")
        void testSave_CircularReferenceInExistingHierarchy_ThrowsException() {
            // Given: Existing circular reference A → B → A
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 5);
            
            Category categoryA = createCategory(1L, "A", 2L); // Points to B
            Category categoryB = createCategory(2L, "B", 1L); // Points to A (circular!)
            
            CategoryRequest request = createRequest(null, "NewChild", 1L);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(categoryA));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(categoryB));

            // When & Then
            assertThatThrownBy(() -> categoryService.save(request))
                    .isInstanceOf(CircularCategoryReferenceException.class)
                    .hasMessageContaining("Circular reference detected");

            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should prevent infinite loop with broken parent chain")
        void testSave_BrokenParentChain_ThrowsException() {
            // Given: Parent chain that doesn't reach root within maxDepth
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 3);
            
            // Create a chain longer than maxDepth without reaching root
            Category level1 = createCategory(1L, "Level1", 2L);
            Category level2 = createCategory(2L, "Level2", 3L);
            Category level3 = createCategory(3L, "Level3", 4L);
            Category level4 = createCategory(4L, "Level4", 5L);
            
            CategoryRequest request = createRequest(null, "NewChild", 1L);

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(level1));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(level2));
            when(categoryRepository.findById(3L)).thenReturn(Optional.of(level3));
            when(categoryRepository.findById(4L)).thenReturn(Optional.of(level4));

            // When & Then
            assertThatThrownBy(() -> categoryService.save(request))
                    .isInstanceOf(CategoryHierarchyDepthExceededException.class)
                    .hasMessageContaining("hierarchy depth");

            verify(categoryRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when parent in chain is missing")
        void testSave_MissingParentInChain_ThrowsException() {
            // Given: Parent chain has missing node
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 5);
            
            Category level2 = createCategory(2L, "Level2", 1L); // Points to ID 1
            CategoryRequest request = createRequest(null, "NewChild", 2L);

            when(categoryRepository.findById(2L)).thenReturn(Optional.of(level2));
            when(categoryRepository.findById(1L)).thenReturn(Optional.empty()); // Missing!

            // When & Then
            assertThatThrownBy(() -> categoryService.save(request))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Parent category not found with id 1");

            verify(categoryRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Different Max Depth Configurations")
    class DifferentMaxDepthConfigurations {

        @Test
        @DisplayName("Should respect max depth of 10")
        void testSave_MaxDepth10_AllowsDepth9() {
            // Given: maxDepth=10, parent at depth 8
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 10);
            
            // Create hierarchy: Root → L1 → L2 → L3 → L4 → L5 → L6 → L7 → L8
            Category root = createCategory(1L, "Root", null);
            Category l1 = createCategory(2L, "L1", 1L);
            Category l2 = createCategory(3L, "L2", 2L);
            Category l3 = createCategory(4L, "L3", 3L);
            Category l4 = createCategory(5L, "L4", 4L);
            Category l5 = createCategory(6L, "L5", 5L);
            Category l6 = createCategory(7L, "L6", 6L);
            Category l7 = createCategory(8L, "L7", 7L);
            Category l8 = createCategory(9L, "L8", 8L); // Depth 8
            
            CategoryRequest request = createRequest(null, "L9", 9L); // Would be depth 9
            Category category = createCategory(null, "L9", 9L);
            Category savedCategory = createCategory(10L, "L9", 9L);
            CategoryResponse response = createResponse(10L, "L9");

            when(categoryRepository.findById(9L)).thenReturn(Optional.of(l8));
            when(categoryRepository.findById(8L)).thenReturn(Optional.of(l7));
            when(categoryRepository.findById(7L)).thenReturn(Optional.of(l6));
            when(categoryRepository.findById(6L)).thenReturn(Optional.of(l5));
            when(categoryRepository.findById(5L)).thenReturn(Optional.of(l4));
            when(categoryRepository.findById(4L)).thenReturn(Optional.of(l3));
            when(categoryRepository.findById(3L)).thenReturn(Optional.of(l2));
            when(categoryRepository.findById(2L)).thenReturn(Optional.of(l1));
            when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));
            when(categoryMapper.toCategory(request)).thenReturn(category);
            when(categoryRepository.save(category)).thenReturn(savedCategory);
            when(categoryMapper.toCategoryResponse(savedCategory)).thenReturn(response);

            // When
            CategoryResponse result = categoryService.save(request);

            // Then
            assertThat(result).isNotNull();
            verify(categoryRepository).save(category);
        }

        @Test
        @DisplayName("Should enforce max depth of 1 (only root categories allowed)")
        void testSave_MaxDepth1_PreventsAnyChildren() {
            // Given: maxDepth=1, trying to create child of root
            ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 1);
            
            Category root = createCategory(1L, "Root", null); // Depth 0
            CategoryRequest request = createRequest(null, "Child", 1L); // Would be depth 1

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));

            // When & Then
            assertThatThrownBy(() -> categoryService.save(request))
                    .isInstanceOf(CategoryHierarchyDepthExceededException.class)
                    .hasMessageContaining("hierarchy depth (1) exceeded maximum allowed depth of 1");

            verify(categoryRepository, never()).save(any());
        }
    }
}
