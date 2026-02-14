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
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("businesses/owner")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Business Owner", description = "Business Owner API - Manage your own businesses (requires authentication)")
@SecurityRequirement(name = "bearerAuth")
public class OwnerBusinessController {

    private final BusinessService businessService;

    @PostMapping
    @Operation(
            summary = "Create a new business",
            description = "Creates a new business owned by the authenticated user. The business will be initially active and assigned to your user account."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Business created successfully",
                    content = @Content(schema = @Schema(implementation = BusinessResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or validation errors",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated - missing or invalid JWT token",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Business with this email already exists",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            )
    })
    public ResponseEntity<BusinessResponse> createBusiness(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Business details to create",
                    required = true
            )
            @Valid @RequestBody BusinessRequest request,
            @Parameter(hidden = true) Authentication connectedUser
    ) {
        log.info("POST /businesses/owner - Creating business: {}", request.name());
        BusinessResponse response = businessService.createBusiness(request, connectedUser);
        log.info("POST /businesses/owner - Business created: id={}", response.getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "Get all my businesses",
            description = "Retrieves a paginated list of all businesses owned by the authenticated user, including inactive businesses."
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
            )
    })
    public ResponseEntity<PageResponse<BusinessResponse>> getMyBusinesses(
            @Parameter(description = "Page number (zero-based)", example = "0")
            @RequestParam(name = "page", defaultValue = "0", required = false) @Min(0) int page,
            @Parameter(description = "Number of items per page", example = "10")
            @RequestParam(name = "size", defaultValue = "10", required = false) @Min(1) int size,
            @Parameter(hidden = true) Authentication connectedUser
    ) {
        log.info("GET /businesses/owner - Retrieving owner's businesses (page={}, size={})", page, size);
        return ResponseEntity.ok(businessService.findByOwner(connectedUser, page, size));
    }

    @GetMapping("{id}")
    @Operation(
            summary = "Get one of my businesses by ID",
            description = "Retrieves detailed information for a specific business owned by the authenticated user."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Business found and returned",
                    content = @Content(schema = @Schema(implementation = BusinessResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Business belongs to another user",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Business not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            )
    })
    public ResponseEntity<BusinessResponse> getMyBusinessById(
            @Parameter(description = "Business identifier", example = "123", required = true)
            @PathVariable @Positive Long id,
            @Parameter(hidden = true) Authentication connectedUser
    ) {
        return ResponseEntity.ok(businessService.findByIdAndOwner(id, connectedUser));
    }

    @PutMapping("{id}")
    @Operation(
            summary = "Update one of my businesses",
            description = "Updates an existing business owned by the authenticated user. All fields in the request will replace existing values."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Business updated successfully",
                    content = @Content(schema = @Schema(implementation = BusinessResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or validation errors",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Business belongs to another user",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Business not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            )
    })
    public ResponseEntity<BusinessResponse> updateMyBusiness(
            @Parameter(description = "Business identifier", example = "123", required = true)
            @PathVariable @Positive Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Updated business details",
                    required = true
            )
            @Valid @RequestBody BusinessRequest request,
            @Parameter(hidden = true) Authentication connectedUser
    ) {
        return ResponseEntity.ok(businessService.updateBusinessByOwner(id, request, connectedUser));
    }

    @PatchMapping("{id}/active")
    @Operation(
            summary = "Toggle business active status",
            description = "Activates or deactivates a business. Inactive businesses are not visible in public API endpoints."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Business status updated successfully",
                    content = @Content(schema = @Schema(implementation = BusinessResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Business belongs to another user",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Business not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            )
    })
    public ResponseEntity<BusinessResponse> toggleBusinessActive(
            @Parameter(description = "Business identifier", example = "123", required = true)
            @PathVariable @Positive Long id,
            @Parameter(description = "New active status (true=active, false=inactive)", example = "true", required = true)
            @RequestParam boolean active,
            @Parameter(hidden = true) Authentication connectedUser
    ) {
        return ResponseEntity.ok(businessService.toggleBusinessActiveByOwner(id, active, connectedUser));
    }

    @DeleteMapping("{id}")
    @Operation(
            summary = "Delete one of my businesses",
            description = "Permanently deletes a business owned by the authenticated user. This action cannot be undone."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Business deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Not authenticated",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Business belongs to another user",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Business not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            )
    })
    public ResponseEntity<Void> deleteMyBusiness(
            @Parameter(description = "Business identifier", example = "123", required = true)
            @PathVariable @Positive Long id,
            @Parameter(hidden = true) Authentication connectedUser
    ) {
        businessService.deleteBusinessByOwner(id, connectedUser);
        return ResponseEntity.noContent().build();
    }
}
