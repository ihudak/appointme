package eu.dec21.appointme.categories.categories.controller;

import eu.dec21.appointme.categories.categories.request.CategoryRequest;
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
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("categories/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Validated
@Slf4j
@Tag(name = "Categories Admin", description = "Admin Categories API - Requires ADMIN role for category management")
@SecurityRequirement(name = "bearerAuth")
public class AdminCategoryController {

    private final CategoryService categoryService;

    @PostMapping
    @Operation(
            summary = "Create a new category (admin)",
            description = "Creates a new category in the hierarchy. Can create root categories (parentId=null) or subcategories. " +
                    "Protected against circular references and enforces maximum hierarchy depth validation."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Category created successfully",
                    content = @Content(schema = @Schema(implementation = CategoryResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request or hierarchy depth exceeded",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "User does not have ADMIN role",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Category with this name already exists",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            )
    })
    public ResponseEntity<CategoryResponse> createCategory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Category details to create", required = true)
            @Valid @RequestBody CategoryRequest request
    ) {
        log.info("POST /categories/admin - Creating category: {}", request.name());
        CategoryResponse response = categoryService.save(request);
        log.info("POST /categories/admin - Category created: id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("{id}")
    @Operation(
            summary = "Get a category by ID (admin)",
            description = "Retrieves any category including inactive ones."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "User does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<CategoryResponse> getCategoryById(
            @Parameter(description = "Category identifier", example = "42", required = true)
            @PathVariable @Positive Long id
    ) {
        log.info("GET /categories/admin/{} - Retrieving category", id);
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @GetMapping
    @Operation(
        summary = "Get all root categories (admin)",
        description = "Retrieves all root categories with optional filter for inactive categories."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Root categories retrieved"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "User does not have ADMIN role")
    })
    public ResponseEntity<PageResponse<CategoryResponse>> getRootCategories(
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(name = "size", defaultValue = "10", required = false) @Min(1) int size,
            @Parameter(description = "Include inactive categories", example = "false")
            @RequestParam(name = "includeInactive", defaultValue = "false", required = false) boolean includeInactive
    ) {
        return ResponseEntity.ok(categoryService.findAllRootCategories(page, size, includeInactive));
    }

    @GetMapping("{parentId}/children")
    @Operation(
        summary = "Get subcategories (admin)",
        description = "Retrieves all direct child categories of a parent with optional filter for inactive categories."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subcategories retrieved"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "User does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Parent category not found")
    })
    public ResponseEntity<PageResponse<CategoryResponse>> getSubCategories(
            @Parameter(description = "Parent category identifier", example = "1", required = true)
            @PathVariable @Positive Long parentId,
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(name = "size", defaultValue = "10", required = false) @Min(1) int size,
            @Parameter(description = "Include inactive categories", example = "false")
            @RequestParam(name = "includeInactive", defaultValue = "false", required = false) boolean includeInactive
    ) {
        return ResponseEntity.ok(categoryService.findAllSubCategories(parentId, page, size, includeInactive));
    }

    @GetMapping("{categoryId}/subcategories/ids")
    @Operation(
        summary = "Get all subcategory IDs recursively (admin)",
        description = "Retrieves all subcategory IDs recursively with optional filter for inactive categories."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Subcategory IDs retrieved"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "User does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public ResponseEntity<Set<Long>> getAllSubcategoryIds(
            @Parameter(description = "Category identifier", example = "1", required = true)
            @PathVariable @Positive Long categoryId,
            @Parameter(description = "Include inactive categories", example = "false")
            @RequestParam(name = "includeInactive", defaultValue = "false", required = false) boolean includeInactive
    ) {
        return ResponseEntity.ok(categoryService.findAllSubcategoryIdsRecursively(categoryId, includeInactive));
    }
}
