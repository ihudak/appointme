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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("categories")
@RequiredArgsConstructor
@Tag(name = "Categories", description = "Categories API")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(summary = "Create a new category", description = "Creates a new category with the provided details")
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
        return ResponseEntity.ok(categoryService.save(request));
    }

    @GetMapping("{id}")
    @Operation(summary = "Get a category by ID", description = "Retrieves a category by its ID")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Get all root categories", description = "Retrieves all root categories (those without a parent)")
    public ResponseEntity<PageResponse<CategoryResponse>> getRootCategories(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) {
        return ResponseEntity.ok(categoryService.findRootCategories(page, size));
    }

    @GetMapping("{parentId}/children")
    @Operation(summary = "Get subcategories", description = "Retrieves all subcategories of a specific parent category")
    public ResponseEntity<PageResponse<CategoryResponse>> getSubCategories(
            @PathVariable Long parentId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) {
        return ResponseEntity.ok(categoryService.findSubCategories(parentId, page, size));
    }
}
