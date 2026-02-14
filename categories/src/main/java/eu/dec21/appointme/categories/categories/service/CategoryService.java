package eu.dec21.appointme.categories.categories.service;

import eu.dec21.appointme.categories.categories.entity.Category;
import eu.dec21.appointme.categories.categories.exception.CategoryHierarchyDepthExceededException;
import eu.dec21.appointme.categories.categories.exception.CircularCategoryReferenceException;
import eu.dec21.appointme.categories.categories.mapper.CategoryMapper;
import eu.dec21.appointme.categories.categories.repository.CategoryRepository;
import eu.dec21.appointme.categories.categories.request.CategoryRequest;
import eu.dec21.appointme.categories.categories.response.CategoryResponse;
import eu.dec21.appointme.common.response.PageResponse;
import eu.dec21.appointme.exceptions.DuplicateResourceException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
    
    /**
     * Maximum depth for category hierarchy traversal.
     * Configurable via application.yaml (category.hierarchy.max-depth)
     * or environment variable (CATEGORY_HIERARCHY_MAX_DEPTH).
     * Default: 5 levels
     */
    @Value("${category.hierarchy.max-depth:5}")
    private int maxHierarchyDepth;


    public CategoryResponse save(CategoryRequest request) {
        // Check for duplicate category name
        if (categoryRepository.existsByName(request.name())) {
            throw new DuplicateResourceException("Category with name '" + request.name() + "' already exists");
        }
        
        // Validate hierarchy depth if parentId is provided
        if (request.parentId() != null) {
            validateHierarchyDepth(request.parentId());
        }
        
        Category category = categoryMapper.toCategory(request);
        return categoryMapper.toCategoryResponse(categoryRepository.save(category));
    }

    public CategoryResponse findById(Long id) {
        return categoryRepository.findById(id).map(categoryMapper::toCategoryResponse).orElseThrow(() -> new EntityNotFoundException("Category not found with id " + id));
    }

    // Public methods - active categories only
    public PageResponse<CategoryResponse> findActiveRootCategories(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Category> categories = categoryRepository.findByParentIsNullAndActiveTrue(pageable);
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

    public PageResponse<CategoryResponse> findActiveSubCategories(Long parentId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Category> categories = categoryRepository.findByParentIdAndActiveTrue(parentId, pageable);
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

    public Set<Long> findAllActiveSubcategoryIdsRecursively(Long categoryId) {
        categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with id " + categoryId));
        
        Set<Long> result = new HashSet<>();
        Set<Long> visited = new HashSet<>();
        collectActiveSubcategoryIds(categoryId, result, visited, 0);
        return result;
    }

    // Admin methods - all categories (with optional filter)
    public PageResponse<CategoryResponse> findAllRootCategories(int page, int size, boolean includeInactive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Category> categories = includeInactive 
            ? categoryRepository.findByParentIsNull(pageable)
            : categoryRepository.findByParentIsNullAndActiveTrue(pageable);
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

    public PageResponse<CategoryResponse> findAllSubCategories(Long parentId, int page, int size, boolean includeInactive) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Category> categories = includeInactive
            ? categoryRepository.findByParentId(parentId, pageable)
            : categoryRepository.findByParentIdAndActiveTrue(parentId, pageable);
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

    public Set<Long> findAllSubcategoryIdsRecursively(Long categoryId, boolean includeInactive) {
        categoryRepository.findById(categoryId)
            .orElseThrow(() -> new EntityNotFoundException("Category not found with id " + categoryId));
        
        Set<Long> result = new HashSet<>();
        Set<Long> visited = new HashSet<>();
        if (includeInactive) {
            collectSubcategoryIds(categoryId, result, visited, 0);
        } else {
            collectActiveSubcategoryIds(categoryId, result, visited, 0);
        }
        return result;
    }

    /**
     * Recursively collect all subcategory IDs (including inactive categories).
     * 
     * @param parentId Current category ID to process
     * @param result Set to accumulate subcategory IDs
     * @param visited Set to track visited categories (prevents circular references)
     * @param depth Current depth in the hierarchy (prevents stack overflow)
     * @throws CircularCategoryReferenceException if a circular reference is detected
     * @throws CategoryHierarchyDepthExceededException if max depth is exceeded
     */
    private void collectSubcategoryIds(Long parentId, Set<Long> result, Set<Long> visited, int depth) {
        // Check maximum depth to prevent stack overflow
        if (depth >= maxHierarchyDepth) {
            throw new CategoryHierarchyDepthExceededException(parentId, maxHierarchyDepth, depth);
        }
        
        // Check for circular reference
        if (visited.contains(parentId)) {
            throw new CircularCategoryReferenceException(parentId, parentId);
        }
        
        visited.add(parentId);
        
        List<Category> children = categoryRepository.findByParentId(parentId);
        for (Category child : children) {
            result.add(child.getId());
            collectSubcategoryIds(child.getId(), result, visited, depth + 1);
        }
    }

    /**
     * Recursively collect active subcategory IDs only.
     * 
     * @param parentId Current category ID to process
     * @param result Set to accumulate subcategory IDs
     * @param visited Set to track visited categories (prevents circular references)
     * @param depth Current depth in the hierarchy (prevents stack overflow)
     * @throws CircularCategoryReferenceException if a circular reference is detected
     * @throws CategoryHierarchyDepthExceededException if max depth is exceeded
     */
    private void collectActiveSubcategoryIds(Long parentId, Set<Long> result, Set<Long> visited, int depth) {
        // Check maximum depth to prevent stack overflow
        if (depth >= maxHierarchyDepth) {
            throw new CategoryHierarchyDepthExceededException(parentId, maxHierarchyDepth, depth);
        }
        
        // Check for circular reference
        if (visited.contains(parentId)) {
            throw new CircularCategoryReferenceException(parentId, parentId);
        }
        
        visited.add(parentId);
        
        List<Category> children = categoryRepository.findByParentIdAndActiveTrue(parentId);
        for (Category child : children) {
            result.add(child.getId());
            collectActiveSubcategoryIds(child.getId(), result, visited, depth + 1);
        }
    }
    
    /**
     * Validates that adding a category under the specified parent won't exceed max hierarchy depth.
     * Calculates depth from root to parent and ensures there's room for one more level.
     * 
     * @param parentId ID of the parent category
     * @throws EntityNotFoundException if parent category doesn't exist
     * @throws CategoryHierarchyDepthExceededException if adding a child would exceed max depth
     */
    private void validateHierarchyDepth(Long parentId) {
        // Verify parent exists
        Category parent = categoryRepository.findById(parentId)
            .orElseThrow(() -> new EntityNotFoundException("Parent category not found with id " + parentId));
        
        // Calculate depth from root to this parent
        int currentDepth = calculateDepthFromRoot(parent);
        
        // Check if we can add one more level (the new category being created)
        // currentDepth is the depth of the parent, so currentDepth + 1 will be the new category's depth
        if (currentDepth + 1 >= maxHierarchyDepth) {
            throw new CategoryHierarchyDepthExceededException(
                parentId, 
                maxHierarchyDepth, 
                currentDepth + 1
            );
        }
    }
    
    /**
     * Calculates the depth of a category from the root of the hierarchy.
     * Root categories (parent = null) have depth 0.
     * Each level down adds 1 to the depth.
     * 
     * @param category The category to calculate depth for
     * @return The depth from root (0 for root categories)
     */
    private int calculateDepthFromRoot(Category category) {
        int depth = 0;
        Category current = category;
        Set<Long> visited = new HashSet<>();
        
        // Traverse up the hierarchy until we reach a root (parent == null)
        while (current.getParent() != null) {
            // Check for circular reference during traversal
            if (visited.contains(current.getId())) {
                throw new CircularCategoryReferenceException(current.getId(), current.getParent().getId());
            }
            visited.add(current.getId());
            
            // Safety check: prevent infinite loop if depth exceeds reasonable limit
            if (depth >= maxHierarchyDepth) {
                throw new CategoryHierarchyDepthExceededException(
                    current.getId(),
                    maxHierarchyDepth,
                    depth
                );
            }
            
            depth++;
            
            // Fetch parent - if it's lazy loaded, we need to explicitly load it
            Long parentId = current.getParent().getId();
            current = categoryRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Parent category not found with id " + parentId
                ));
        }
        
        return depth;
    }
}
