package eu.dec21.appointme.categories.categories.controller;

import eu.dec21.appointme.categories.categories.response.CategoryResponse;
import eu.dec21.appointme.categories.categories.service.CategoryService;
import eu.dec21.appointme.common.response.PageResponse;
import eu.dec21.appointme.exceptions.handler.ExceptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("categories")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Categories", description = "Public Categories API - Active categories only")
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping("{id}")
    @Operation(
            summary = "Get a category by ID",
            description = "Retrieves detailed information for a single active category."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Category found and returned successfully",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category not found or inactive",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            )
    })
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Category identifier", example = "42", required = true)
            @PathVariable @Positive Long id
    ) {
        log.info("GET /categories/{} - Retrieving category", id);
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @GetMapping
    @Operation(
            summary = "Get all root categories",
            description = "Retrieves a paginated list of all active root categories (categories without a parent). " +
                    "These represent the top level of the category hierarchy."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Root categories retrieved successfully (may be empty)",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            )
    })
    public ResponseEntity<PageResponse<CategoryResponse>> getRootCategories(
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(name = "size", defaultValue = "10", required = false) @Min(1) int size
    ) {
        log.info("GET /categories - Retrieving root categories (page={}, size={})", page, size);
        return ResponseEntity.ok(categoryService.findActiveRootCategories(page, size));
    }

    @GetMapping("{parentId}/children")
    @Operation(
            summary = "Get direct subcategories",
            description = "Retrieves all active direct child categories of a specific parent category (one level down only)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Subcategories retrieved successfully (may be empty if no children exist)",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Parent category not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            )
    })
    public ResponseEntity<PageResponse<CategoryResponse>> getSubCategories(
            @Parameter(description = "Parent category identifier", example = "1", required = true)
            @PathVariable @Positive Long parentId,
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(name = "size", defaultValue = "10", required = false) @Min(1) int size
    ) {
        return ResponseEntity.ok(categoryService.findActiveSubCategories(parentId, page, size));
    }

    @GetMapping("{categoryId}/subcategories/ids")
    @Operation(
        summary = "Get all subcategory IDs recursively",
        description = "Retrieves a flat set of all active subcategory IDs (children, grandchildren, great-grandchildren, etc.) " +
                "for a given category. Useful for filtering businesses by category hierarchy. " +
                "Protected against circular references."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Subcategory IDs retrieved successfully",
                    content = @Content(schema = @Schema(type = "array", implementation = Long.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            )
    })
    public ResponseEntity<Set<Long>> getAllSubcategoryIds(
            @Parameter(description = "Category identifier", example = "1", required = true)
            @PathVariable @Positive Long categoryId
    ) {
        return ResponseEntity.ok(categoryService.findAllActiveSubcategoryIdsRecursively(categoryId));
    }
}
