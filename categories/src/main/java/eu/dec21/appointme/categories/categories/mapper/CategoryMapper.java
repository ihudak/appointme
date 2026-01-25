package eu.dec21.appointme.categories.categories.mapper;

import eu.dec21.appointme.categories.categories.entity.Category;
import eu.dec21.appointme.categories.categories.repository.CategoryRepository;
import eu.dec21.appointme.categories.categories.request.CategoryRequest;
import eu.dec21.appointme.categories.categories.response.CategoryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryMapper {

    private final CategoryRepository categoryRepository;

    public Category toCategory(CategoryRequest request) {

        Category parentCategory = request.parentId() != null ? categoryRepository.findById(request.parentId()).orElse(null) : null;

        return Category.builder()
                .id(request.id())
                .name(request.name())
                .description(request.description())
                .parent(parentCategory)
                .active(true)
                .build();
    }

    public CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .active(category.isActive())
                .imageUrl(category.getImageUrl())
                .build();
    }
}
