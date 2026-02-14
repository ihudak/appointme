package eu.dec21.appointme.businesses.businesses.controller;

import eu.dec21.appointme.businesses.businesses.request.BusinessRequest;
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

@RestController
@RequestMapping("businesses/admin")
@RequiredArgsConstructor
@Validated
@PreAuthorize("hasRole('ADMIN')")
@Slf4j
@Tag(name = "Business Admin", description = "Admin Business API - Requires ADMIN role for moderation and management")
@SecurityRequirement(name = "bearerAuth")
public class AdminBusinessController {

    private final BusinessService businessService;

    @GetMapping
    @Operation(
        summary = "Get all businesses (admin)",
        description = "Retrieves all businesses with optional filter for inactive businesses. Admins can see all businesses regardless of active status."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Businesses retrieved successfully",
                    content = @Content(schema = @Schema(implementation = PageResponse.class))
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
            )
    })
    public ResponseEntity<PageResponse<BusinessResponse>> getAllBusinesses(
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(name = "size", defaultValue = "10", required = false) @Min(1) int size,
            @Parameter(description = "Include inactive businesses in results", example = "false")
            @RequestParam(name = "includeInactive", defaultValue = "false", required = false) boolean includeInactive
    ) {
        log.info("GET /businesses/admin - Retrieving all businesses (page={}, size={}, includeInactive={})", page, size, includeInactive);
        return ResponseEntity.ok(businessService.findAllBusinesses(page, size, includeInactive));
    }

    @GetMapping("{id}")
    @Operation(
            summary = "Get any business by ID (admin)",
            description = "Retrieves any business including inactive ones. For moderation and management purposes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Business found"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "User does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Business not found")
    })
    public ResponseEntity<BusinessResponse> getBusinessById(
            @Parameter(description = "Business identifier", example = "123", required = true)
            @PathVariable @Positive Long id
    ) {
        return ResponseEntity.ok(businessService.findByIdAdmin(id));
    }

    @PutMapping("{id}")
    @Operation(
            summary = "Update any business (admin)",
            description = "Updates any business regardless of ownership. For moderation purposes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Business updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request body"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "User does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Business not found")
    })
    public ResponseEntity<BusinessResponse> updateBusiness(
            @Parameter(description = "Business identifier", example = "123", required = true)
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated business details")
            @Valid @RequestBody BusinessRequest request
    ) {
        return ResponseEntity.ok(businessService.updateBusinessByAdmin(id, request));
    }

    @PatchMapping("{id}/active")
    @Operation(
            summary = "Toggle business active status (admin)",
            description = "Activates or deactivates any business. Use to block/unblock businesses for moderation."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Business status updated"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "User does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Business not found")
    })
    public ResponseEntity<BusinessResponse> toggleBusinessActive(
            @Parameter(description = "Business identifier", example = "123", required = true)
            @PathVariable @Positive Long id,
            @Parameter(description = "New active status", example = "false", required = true)
            @RequestParam boolean active
    ) {
        return ResponseEntity.ok(businessService.toggleBusinessActiveByAdmin(id, active));
    }

    @DeleteMapping("{id}")
    @Operation(
            summary = "Delete any business (admin)",
            description = "Permanently deletes any business. This action cannot be undone."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Business deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Not authenticated"),
            @ApiResponse(responseCode = "403", description = "User does not have ADMIN role"),
            @ApiResponse(responseCode = "404", description = "Business not found")
    })
    public ResponseEntity<Void> deleteBusiness(
            @Parameter(description = "Business identifier", example = "123", required = true)
            @PathVariable @Positive Long id
    ) {
        businessService.deleteBusinessByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
