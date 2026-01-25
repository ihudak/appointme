package eu.dec21.appointme.categories.categories.service;

import eu.dec21.appointme.categories.categories.entity.Category;
import eu.dec21.appointme.categories.categories.mapper.CategoryMapper;
import eu.dec21.appointme.categories.categories.repository.CategoryRepository;
import eu.dec21.appointme.categories.categories.request.CategoryRequest;
import eu.dec21.appointme.categories.categories.response.CategoryResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;
    private final CategoryRepository categoryRepository;


    public CategoryResponse save(CategoryRequest request) {
        Category category = categoryMapper.toCategory(request);
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    public CategoryResponse findById(Long id) {
        return categoryRepository.findById(id).map(categoryMapper::toCategoryResponse).orElseThrow(() -> new EntityNotFoundException("Category not found with id " + id"));
    }
}
