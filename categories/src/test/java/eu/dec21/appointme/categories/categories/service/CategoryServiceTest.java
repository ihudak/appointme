package eu.dec21.appointme.categories.categories.service;

import eu.dec21.appointme.categories.categories.entity.Category;
import eu.dec21.appointme.categories.categories.mapper.CategoryMapper;
import eu.dec21.appointme.categories.categories.repository.CategoryRepository;
import eu.dec21.appointme.categories.categories.request.CategoryRequest;
import eu.dec21.appointme.categories.categories.response.CategoryResponse;
import eu.dec21.appointme.common.response.PageResponse;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryMapper categoryMapper;
    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private CategoryService categoryService;

    private Category createCategory(Long id, String name) {
        return Category.builder().id(id).name(name).active(true).build();
    }

    private CategoryResponse createResponse(Long id, String name) {
        return CategoryResponse.builder().id(id).name(name).active(true).build();
    }

    // === save ===

    @Test
    void save_delegatesToMapperAndRepository() {
        CategoryRequest request = new CategoryRequest(null, "Test", "desc", null);
        Category entity = createCategory(1L, "Test");
        CategoryResponse response = createResponse(1L, "Test");

        when(categoryMapper.toCategory(request)).thenReturn(entity);
        when(categoryRepository.save(entity)).thenReturn(entity);
        when(categoryMapper.toCategoryResponse(entity)).thenReturn(response);

        CategoryResponse result = categoryService.save(request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test");
        verify(categoryRepository).save(entity);
    }

    // === findById ===

    @Test
    void findById_existing_returnsResponse() {
        Category entity = createCategory(1L, "Cat");
        CategoryResponse response = createResponse(1L, "Cat");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(entity));
        when(categoryMapper.toCategoryResponse(entity)).thenReturn(response);

        CategoryResponse result = categoryService.findById(1L);
        assertThat(result.getName()).isEqualTo("Cat");
    }

    @Test
    void findById_notFound_throwsEntityNotFoundException() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(999L))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("999");
    }

    // === findActiveRootCategories ===

    @Test
    void findActiveRootCategories_returnsPageResponse() {
        Category cat = createCategory(1L, "Root");
        CategoryResponse resp = createResponse(1L, "Root");
        Page<Category> page = new PageImpl<>(List.of(cat), PageRequest.of(0, 10), 1);

        when(categoryRepository.findByParentIsNullAndActiveTrue(any(Pageable.class))).thenReturn(page);
        when(categoryMapper.toCategoryResponse(cat)).thenReturn(resp);

        PageResponse<CategoryResponse> result = categoryService.findActiveRootCategories(0, 10);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.isFirst()).isTrue();
    }

    @Test
    void findActiveRootCategories_emptyResult() {
        Page<Category> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(categoryRepository.findByParentIsNullAndActiveTrue(any(Pageable.class))).thenReturn(emptyPage);

        PageResponse<CategoryResponse> result = categoryService.findActiveRootCategories(0, 10);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(0);
    }

    // === findActiveSubCategories ===

    @Test
    void findActiveSubCategories_returnsPageResponse() {
        Category sub = createCategory(2L, "Sub");
        CategoryResponse resp = createResponse(2L, "Sub");
        Page<Category> page = new PageImpl<>(List.of(sub), PageRequest.of(0, 10), 1);

        when(categoryRepository.findByParentIdAndActiveTrue(eq(1L), any(Pageable.class))).thenReturn(page);
        when(categoryMapper.toCategoryResponse(sub)).thenReturn(resp);

        PageResponse<CategoryResponse> result = categoryService.findActiveSubCategories(1L, 0, 10);

        assertThat(result.getContent()).hasSize(1);
    }

    // === findAllActiveSubcategoryIdsRecursively ===

    @Test
    void findAllActiveSubcategoryIdsRecursively_categoryNotFound_throws() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findAllActiveSubcategoryIdsRecursively(999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void findAllActiveSubcategoryIdsRecursively_noChildren_returnsEmpty() {
        ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 10);
        
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(createCategory(1L, "Root")));
        when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of());

        Set<Long> result = categoryService.findAllActiveSubcategoryIdsRecursively(1L);
        assertThat(result).isEmpty();
    }

    @Test
    void findAllActiveSubcategoryIdsRecursively_withNestedChildren() {
        ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 10);
        
        Category child = createCategory(2L, "Child");
        Category grandchild = createCategory(3L, "Grandchild");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(createCategory(1L, "Root")));
        when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of(child));
        when(categoryRepository.findByParentIdAndActiveTrue(2L)).thenReturn(List.of(grandchild));
        when(categoryRepository.findByParentIdAndActiveTrue(3L)).thenReturn(List.of());

        Set<Long> result = categoryService.findAllActiveSubcategoryIdsRecursively(1L);
        assertThat(result).containsExactlyInAnyOrder(2L, 3L);
    }

    // === findAllRootCategories (admin) ===

    @Test
    void findAllRootCategories_includeInactive_usesCorrectQuery() {
        Page<Category> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(categoryRepository.findByParentIsNull(any(Pageable.class))).thenReturn(page);

        categoryService.findAllRootCategories(0, 10, true);

        verify(categoryRepository).findByParentIsNull(any(Pageable.class));
        verify(categoryRepository, never()).findByParentIsNullAndActiveTrue(any(Pageable.class));
    }

    @Test
    void findAllRootCategories_activeOnly_usesCorrectQuery() {
        Page<Category> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(categoryRepository.findByParentIsNullAndActiveTrue(any(Pageable.class))).thenReturn(page);

        categoryService.findAllRootCategories(0, 10, false);

        verify(categoryRepository).findByParentIsNullAndActiveTrue(any(Pageable.class));
        verify(categoryRepository, never()).findByParentIsNull(any(Pageable.class));
    }

    // === findAllSubCategories (admin) ===

    @Test
    void findAllSubCategories_includeInactive_usesCorrectQuery() {
        Page<Category> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(categoryRepository.findByParentId(eq(1L), any(Pageable.class))).thenReturn(page);

        categoryService.findAllSubCategories(1L, 0, 10, true);

        verify(categoryRepository).findByParentId(eq(1L), any(Pageable.class));
    }

    @Test
    void findAllSubCategories_activeOnly_usesCorrectQuery() {
        Page<Category> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(categoryRepository.findByParentIdAndActiveTrue(eq(1L), any(Pageable.class))).thenReturn(page);

        categoryService.findAllSubCategories(1L, 0, 10, false);

        verify(categoryRepository).findByParentIdAndActiveTrue(eq(1L), any(Pageable.class));
    }

    // === findAllSubcategoryIdsRecursively (admin) ===

    @Test
    void findAllSubcategoryIdsRecursively_includeInactive() {
        ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 10);
        
        Category child = createCategory(2L, "Child");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(createCategory(1L, "Root")));
        when(categoryRepository.findByParentId(1L)).thenReturn(List.of(child));
        when(categoryRepository.findByParentId(2L)).thenReturn(List.of());

        Set<Long> result = categoryService.findAllSubcategoryIdsRecursively(1L, true);
        assertThat(result).containsExactly(2L);
    }

    @Test
    void findAllSubcategoryIdsRecursively_activeOnly() {
        ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 10);
        
        Category child = createCategory(2L, "Child");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(createCategory(1L, "Root")));
        when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of(child));
        when(categoryRepository.findByParentIdAndActiveTrue(2L)).thenReturn(List.of());

        Set<Long> result = categoryService.findAllSubcategoryIdsRecursively(1L, false);
        assertThat(result).containsExactly(2L);
    }

    @Test
    void findAllSubcategoryIdsRecursively_notFound_throws() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findAllSubcategoryIdsRecursively(999L, true))
                .isInstanceOf(EntityNotFoundException.class);
    }

    // === Edge cases for recursive methods ===

    @Test
    void findAllActiveSubcategoryIdsRecursively_deepHierarchy_collectsAll() {
        ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 10);
        
        Category level1 = createCategory(2L, "Level1");
        Category level2 = createCategory(3L, "Level2");
        Category level3 = createCategory(4L, "Level3");
        Category level4 = createCategory(5L, "Level4");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(createCategory(1L, "Root")));
        when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of(level1));
        when(categoryRepository.findByParentIdAndActiveTrue(2L)).thenReturn(List.of(level2));
        when(categoryRepository.findByParentIdAndActiveTrue(3L)).thenReturn(List.of(level3));
        when(categoryRepository.findByParentIdAndActiveTrue(4L)).thenReturn(List.of(level4));
        when(categoryRepository.findByParentIdAndActiveTrue(5L)).thenReturn(List.of());

        Set<Long> result = categoryService.findAllActiveSubcategoryIdsRecursively(1L);
        assertThat(result).containsExactlyInAnyOrder(2L, 3L, 4L, 5L);
    }

    @Test
    void findAllActiveSubcategoryIdsRecursively_multipleChildrenPerLevel() {
        ReflectionTestUtils.setField(categoryService, "maxHierarchyDepth", 10);
        
        Category child1 = createCategory(2L, "Child1");
        Category child2 = createCategory(3L, "Child2");
        Category grandchild1 = createCategory(4L, "Grandchild1");
        Category grandchild2 = createCategory(5L, "Grandchild2");

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(createCategory(1L, "Root")));
        when(categoryRepository.findByParentIdAndActiveTrue(1L)).thenReturn(List.of(child1, child2));
        when(categoryRepository.findByParentIdAndActiveTrue(2L)).thenReturn(List.of(grandchild1));
        when(categoryRepository.findByParentIdAndActiveTrue(3L)).thenReturn(List.of(grandchild2));
        when(categoryRepository.findByParentIdAndActiveTrue(4L)).thenReturn(List.of());
        when(categoryRepository.findByParentIdAndActiveTrue(5L)).thenReturn(List.of());

        Set<Long> result = categoryService.findAllActiveSubcategoryIdsRecursively(1L);
        assertThat(result).containsExactlyInAnyOrder(2L, 3L, 4L, 5L);
    }

    // === PageResponse field verification ===

    @Test
    void findActiveRootCategories_verifyAllPageResponseFields() {
        Category cat1 = createCategory(1L, "Cat1");
        Category cat2 = createCategory(2L, "Cat2");
        CategoryResponse resp1 = createResponse(1L, "Cat1");
        CategoryResponse resp2 = createResponse(2L, "Cat2");
        Page<Category> page = new PageImpl<>(List.of(cat1, cat2), PageRequest.of(0, 10), 2);

        when(categoryRepository.findByParentIsNullAndActiveTrue(any(Pageable.class))).thenReturn(page);
        when(categoryMapper.toCategoryResponse(cat1)).thenReturn(resp1);
        when(categoryMapper.toCategoryResponse(cat2)).thenReturn(resp2);

        PageResponse<CategoryResponse> result = categoryService.findActiveRootCategories(0, 10);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);
        assertThat(result.getPageNumber()).isEqualTo(0);
        assertThat(result.getPageSize()).isEqualTo(10);
        assertThat(result.isLast()).isTrue();
        assertThat(result.isEmpty()).isFalse();
    }
}
