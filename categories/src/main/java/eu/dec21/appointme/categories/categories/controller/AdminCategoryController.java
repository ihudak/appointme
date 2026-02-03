package eu.dec21.appointme.categories.categories.controller;

import eu.dec21.appointme.categories.categories.request.CategoryRequest;
import eu.dec21.appointme.categories.categories.response.CategoryResponse;
import eu.dec21.appointme.categories.categories.service.CategoryService;
import eu.dec21.appointme.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("categories/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Categories Admin", description = "Admin Categories API - Requires ADMIN role")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a new category", description = "Creates a new category with the provided details")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.save(request));
    }

    @GetMapping("{id}")
    @Operation(summary = "Get a category by ID", description = "Retrieves a category by its ID (including inactive)")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @GetMapping
    @Operation(
        summary = "Get all root categories",
        description = "Retrieves all root categories. Set includeInactive=true to see inactive categories."
    )
    public ResponseEntity<PageResponse<CategoryResponse>> getRootCategories(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            @RequestParam(name = "includeInactive", defaultValue = "false", required = false) boolean includeInactive
    ) {
        return ResponseEntity.ok(categoryService.findAllRootCategories(page, size, includeInactive));
    }

    @GetMapping("{parentId}/children")
    @Operation(
        summary = "Get subcategories",
        description = "Retrieves all subcategories of a specific parent category. Set includeInactive=true to see inactive categories."
    )
    public ResponseEntity<PageResponse<CategoryResponse>> getSubCategories(
            @PathVariable Long parentId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            @RequestParam(name = "includeInactive", defaultValue = "false", required = false) boolean includeInactive
    ) {
        return ResponseEntity.ok(categoryService.findAllSubCategories(parentId, page, size, includeInactive));
    }

    @GetMapping("{categoryId}/subcategories/ids")
    @Operation(
        summary = "Get all subcategory IDs recursively",
        description = "Retrieves all subcategory IDs (children, grandchildren, etc.) of a category. Set includeInactive=true to see inactive categories."
    )
    public ResponseEntity<Set<Long>> getAllSubcategoryIds(
            @PathVariable Long categoryId,
            @RequestParam(name = "includeInactive", defaultValue = "false", required = false) boolean includeInactive
    ) {
        return ResponseEntity.ok(categoryService.findAllSubcategoryIdsRecursively(categoryId, includeInactive));
    }
}
