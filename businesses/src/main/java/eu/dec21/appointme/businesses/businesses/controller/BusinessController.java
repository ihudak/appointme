package eu.dec21.appointme.businesses.businesses.controller;

import eu.dec21.appointme.businesses.businesses.response.BusinessResponse;
import eu.dec21.appointme.businesses.businesses.service.BusinessService;
import eu.dec21.appointme.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Get a business by ID", description = "Retrieves an active business by its ID")
    public ResponseEntity<BusinessResponse> getBusinessById(@PathVariable @Positive Long id) {
        log.info("GET /businesses/{} - Retrieving business", id);
        return ResponseEntity.ok(businessService.findById(id));
    }

    @GetMapping
    @Operation(summary = "Get all businesses", description = "Retrieves all active businesses sorted by weighted rating")
    public ResponseEntity<PageResponse<BusinessResponse>> getAllBusinesses(
            @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) @Min(1) int size
    ) {
        log.info("GET /businesses - Retrieving all businesses (page={}, size={})", page, size);
        return ResponseEntity.ok(businessService.findAll(page, size));
    }

    @GetMapping("category/{categoryId}")
    @Operation(summary = "Get businesses by category", description = "Retrieves all active businesses in a specific category")
    public ResponseEntity<PageResponse<BusinessResponse>> getBusinessesByCategory(
            @PathVariable @Positive Long categoryId,
            @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) @Min(1) int size
    ) {
        return ResponseEntity.ok(businessService.findByCategory(categoryId, page, size));
    }

    @GetMapping("category/{categoryId}/with-subcategories")
    @Operation(
        summary = "Get businesses by category including subcategories",
        description = "Retrieves all active businesses in a category and all its subcategories (recursive hierarchy)"
    )
    public ResponseEntity<PageResponse<BusinessResponse>> getBusinessesByCategoryWithSubcategories(
            @PathVariable @Positive Long categoryId,
            @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) @Min(1) int size
    ) {
        return ResponseEntity.ok(businessService.findByCategoryWithSubcategories(categoryId, page, size));
    }
}
