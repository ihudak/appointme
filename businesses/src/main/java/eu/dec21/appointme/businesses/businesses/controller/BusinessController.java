package eu.dec21.appointme.businesses.businesses.controller;

import eu.dec21.appointme.businesses.businesses.response.BusinessResponse;
import eu.dec21.appointme.businesses.businesses.service.BusinessService;
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

@RestController
@RequestMapping("businesses")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Businesses", description = "Public Businesses API - Active businesses only")
public class BusinessController {

    private final BusinessService businessService;

    @GetMapping("{id}")
    @Operation(
            summary = "Get a business by ID",
            description = "Retrieves detailed information for an active business by its unique identifier. " +
                    "Only returns active businesses - inactive businesses return 404."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Business found and returned successfully",
                    content = @Content(schema = @Schema(implementation = BusinessResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Business not found or inactive",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            )
    })
    public ResponseEntity<BusinessResponse> getBusinessById(
            @Parameter(description = "Unique business identifier", example = "123", required = true)
            @PathVariable @Positive Long id
    ) {
        log.info("GET /businesses/{} - Retrieving business", id);
        return ResponseEntity.ok(businessService.findById(id));
    }

    @GetMapping
    @Operation(
            summary = "Get all active businesses",
            description = "Retrieves a paginated list of all active businesses sorted by weighted rating (highest first). " +
                    "Pagination is zero-based (first page is page=0)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Businesses retrieved successfully (may be empty)",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            )
    })
    public ResponseEntity<PageResponse<BusinessResponse>> getAllBusinesses(
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int page,
            @Parameter(description = "Number of items per page (1-100)", example = "10")
            @RequestParam(name = "size", defaultValue = "10", required = false) @Min(1) int size
    ) {
        log.info("GET /businesses - Retrieving all businesses (page={}, size={})", page, size);
        return ResponseEntity.ok(businessService.findAll(page, size));
    }

    @GetMapping("category/{categoryId}")
    @Operation(
            summary = "Get businesses by category",
            description = "Retrieves all active businesses in a specific category (direct category assignment only, " +
                    "does not include subcategories). Paginated and sorted by weighted rating."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Businesses retrieved successfully (may be empty if no businesses in this category)",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            )
    })
    public ResponseEntity<PageResponse<BusinessResponse>> getBusinessesByCategory(
            @Parameter(description = "Category identifier", example = "42", required = true)
            @PathVariable @Positive Long categoryId,
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int page,
            @Parameter(description = "Number of items per page (1-100)", example = "10")
            @RequestParam(name = "size", defaultValue = "10", required = false) @Min(1) int size
    ) {
        return ResponseEntity.ok(businessService.findByCategory(categoryId, page, size));
    }

    @GetMapping("category/{categoryId}/with-subcategories")
    @Operation(
            summary = "Get businesses by category including all subcategories",
            description = "Retrieves all active businesses in the specified category AND all its subcategories recursively. " +
                    "For example, searching 'Restaurants' will include 'Italian Restaurants', 'Japanese Restaurants', etc. " +
                    "Paginated and sorted by weighted rating."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Businesses retrieved successfully from category hierarchy",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Category not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            )
    })
    public ResponseEntity<PageResponse<BusinessResponse>> getBusinessesByCategoryWithSubcategories(
            @Parameter(description = "Category identifier", example = "42", required = true)
            @PathVariable @Positive Long categoryId,
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int page,
            @Parameter(description = "Number of items per page (1-100)", example = "10")
            @RequestParam(name = "size", defaultValue = "10", required = false) @Min(1) int size
    ) {
        return ResponseEntity.ok(businessService.findByCategoryWithSubcategories(categoryId, page, size));
    }
}
