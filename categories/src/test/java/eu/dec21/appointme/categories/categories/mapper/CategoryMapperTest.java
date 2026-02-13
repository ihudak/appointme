package eu.dec21.appointme.categories.categories.mapper;

import eu.dec21.appointme.categories.categories.entity.Category;
import eu.dec21.appointme.categories.categories.repository.CategoryRepository;
import eu.dec21.appointme.categories.categories.request.CategoryRequest;
import eu.dec21.appointme.categories.categories.response.CategoryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryMapperTest {

    @Mock
    private CategoryRepository categoryRepository;
    @InjectMocks
    private CategoryMapper categoryMapper;

    // === toCategory ===

    @Test
    void toCategory_withoutParent() {
        CategoryRequest request = new CategoryRequest(null, "Test", "desc", null);

        Category result = categoryMapper.toCategory(request);

        assertThat(result.getName()).isEqualTo("Test");
        assertThat(result.getDescription()).isEqualTo("desc");
        assertThat(result.getParent()).isNull();
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void toCategory_withParent() {
        Category parent = Category.builder().id(10L).name("Parent").build();
        when(categoryRepository.findById(10L)).thenReturn(Optional.of(parent));

        CategoryRequest request = new CategoryRequest(null, "Child", "child desc", 10L);

        Category result = categoryMapper.toCategory(request);

        assertThat(result.getName()).isEqualTo("Child");
        assertThat(result.getParent()).isEqualTo(parent);
    }

    @Test
    void toCategory_withNonExistentParent_setsParentNull() {
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        CategoryRequest request = new CategoryRequest(null, "Orphan", null, 999L);

        Category result = categoryMapper.toCategory(request);

        assertThat(result.getParent()).isNull();
    }

    @Test
    void toCategory_setsIdFromRequest() {
        CategoryRequest request = new CategoryRequest(5L, "Test", null, null);

        Category result = categoryMapper.toCategory(request);

        assertThat(result.getId()).isEqualTo(5L);
    }

    @Test
    void toCategory_alwaysSetsActiveTrue() {
        CategoryRequest request = new CategoryRequest(null, "Test", null, null);

        Category result = categoryMapper.toCategory(request);

        assertThat(result.isActive()).isTrue();
    }

    // === toCategoryResponse ===

    @Test
    void toCategoryResponse_withParent() {
        Category parent = Category.builder().id(10L).name("Parent").build();
        Category category = Category.builder()
                .id(1L)
                .name("Test")
                .description("desc")
                .parent(parent)
                .active(true)
                .imageUrl("http://example.com/img.jpg")
                .build();

        CategoryResponse result = categoryMapper.toCategoryResponse(category);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test");
        assertThat(result.getDescription()).isEqualTo("desc");
        assertThat(result.getParentId()).isEqualTo(10L);
        assertThat(result.isActive()).isTrue();
        assertThat(result.getImageUrl()).isEqualTo("http://example.com/img.jpg");
    }

    @Test
    void toCategoryResponse_withoutParent() {
        Category category = Category.builder()
                .id(1L)
                .name("Root")
                .active(true)
                .build();

        CategoryResponse result = categoryMapper.toCategoryResponse(category);

        assertThat(result.getParentId()).isNull();
    }

    @Test
    void toCategoryResponse_withNullDescription() {
        Category category = Category.builder()
                .id(1L)
                .name("Test")
                .description(null)
                .active(false)
                .build();

        CategoryResponse result = categoryMapper.toCategoryResponse(category);

        assertThat(result.getDescription()).isNull();
        assertThat(result.isActive()).isFalse();
    }
}
