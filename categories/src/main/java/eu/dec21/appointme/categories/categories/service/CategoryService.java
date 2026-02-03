package eu.dec21.appointme.categories.categories.service;

import eu.dec21.appointme.categories.categories.entity.Category;
import eu.dec21.appointme.categories.categories.mapper.CategoryMapper;
import eu.dec21.appointme.categories.categories.repository.CategoryRepository;
import eu.dec21.appointme.categories.categories.request.CategoryRequest;
import eu.dec21.appointme.categories.categories.response.CategoryResponse;
import eu.dec21.appointme.common.response.PageResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
        return categoryRepository.findById(id).map(categoryMapper::toCategoryResponse).orElseThrow(() -> new EntityNotFoundException("Category not found with id " + id));
    }

    public PageResponse<CategoryResponse> findRootCategories(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Category> categories = categoryRepository.findByParentIsNull(pageable);
        return new PageResponse<>(
            categories.getContent().stream().map(categoryMapper::toCategoryResponse).toList(),
            categories.getTotalElements(),
            categories.getTotalPages(),
            categories.getNumber(),
            categories.getSize(),
            categories.isLast(),
            categories.isEmpty()
        );
    }

    public PageResponse<CategoryResponse> findSubCategories(Long parentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Category> categories = categoryRepository.findByParentId(parentId, pageable);
        return new PageResponse<>(
            categories.getContent().stream().map(categoryMapper::toCategoryResponse).toList(),
            categories.getTotalElements(),
            categories.getTotalPages(),
            categories.getNumber(),
            categories.getSize(),
            categories.isLast(),
            categories.isEmpty()
        );
    }

    public Set<Long> findAllSubcategoryIdsRecursively(Long categoryId) {
        categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with id " + categoryId));
        
        Set<Long> result = new HashSet<>();
        collectSubcategoryIds(categoryId, result);
        return result;
    }

    private void collectSubcategoryIds(Long parentId, Set<Long> result) {
        List<Category> children = categoryRepository.findByParentId(parentId);
        for (Category child : children) {
            result.add(child.getId());
            collectSubcategoryIds(child.getId(), result);
        }
    }
}
