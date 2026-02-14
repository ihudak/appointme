package eu.dec21.appointme.categories.categories.controller;

import eu.dec21.appointme.categories.categories.response.CategoryResponse;
import eu.dec21.appointme.categories.categories.service.CategoryService;
import eu.dec21.appointme.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Categories", description = "Public Categories API - Active categories only")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("{id}")
    @Operation(summary = "Get a category by ID", description = "Retrieves a category by its ID")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        log.info("GET /categories/{} - Retrieving category", id);
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Get all root categories", description = "Retrieves all active root categories (those without a parent)")
    public ResponseEntity<PageResponse<CategoryResponse>> getRootCategories(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) {
        log.info("GET /categories - Retrieving root categories (page={}, size={})", page, size);
        return ResponseEntity.ok(categoryService.findActiveRootCategories(page, size));
    }

    @GetMapping("{parentId}/children")
    @Operation(summary = "Get subcategories", description = "Retrieves all active subcategories of a specific parent category")
    public ResponseEntity<PageResponse<CategoryResponse>> getSubCategories(
            @PathVariable Long parentId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) {
        return ResponseEntity.ok(categoryService.findActiveSubCategories(parentId, page, size));
    }

    @GetMapping("{categoryId}/subcategories/ids")
    @Operation(
        summary = "Get all subcategory IDs recursively",
        description = "Retrieves all active subcategory IDs (children, grandchildren, etc.) of a category"
    )
    public ResponseEntity<Set<Long>> getAllSubcategoryIds(@PathVariable Long categoryId) {
        return ResponseEntity.ok(categoryService.findAllActiveSubcategoryIdsRecursively(categoryId));
    }
}
