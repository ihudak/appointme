package eu.dec21.appointme.businesses.businesses.controller;

import eu.dec21.appointme.businesses.businesses.request.BusinessRequest;
import eu.dec21.appointme.businesses.businesses.response.BusinessResponse;
import eu.dec21.appointme.businesses.businesses.service.BusinessService;
import eu.dec21.appointme.common.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("businesses/owner")
@RequiredArgsConstructor
@Tag(name = "Business Owner", description = "Business Owner API - Manage your own businesses")
public class OwnerBusinessController {

    private final BusinessService businessService;

    @PostMapping
    @Operation(summary = "Create a new business", description = "Creates a new business owned by the authenticated user")
    public ResponseEntity<BusinessResponse> createBusiness(
            @Valid @RequestBody BusinessRequest request,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(businessService.createBusiness(request, connectedUser));
    }

    @GetMapping
    @Operation(summary = "Get my businesses", description = "Retrieves all businesses owned by the authenticated user (including inactive)")
    public ResponseEntity<PageResponse<BusinessResponse>> getMyBusinesses(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(businessService.findByOwner(connectedUser, page, size));
    }

    @GetMapping("{id}")
    @Operation(summary = "Get my business by ID", description = "Retrieves a business by ID if owned by the authenticated user")
    public ResponseEntity<BusinessResponse> getMyBusinessById(
            @PathVariable Long id,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(businessService.findByIdAndOwner(id, connectedUser));
    }

    @PutMapping("{id}")
    @Operation(summary = "Update my business", description = "Updates a business if owned by the authenticated user")
    public ResponseEntity<BusinessResponse> updateMyBusiness(
            @PathVariable Long id,
            @Valid @RequestBody BusinessRequest request,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(businessService.updateBusinessByOwner(id, request, connectedUser));
    }

    @PatchMapping("{id}/active")
    @Operation(summary = "Toggle business active status", description = "Activates or deactivates a business if owned by the authenticated user")
    public ResponseEntity<BusinessResponse> toggleBusinessActive(
            @PathVariable Long id,
            @RequestParam boolean active,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(businessService.toggleBusinessActiveByOwner(id, active, connectedUser));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete my business", description = "Deletes a business if owned by the authenticated user")
    public ResponseEntity<Void> deleteMyBusiness(
            @PathVariable Long id,
            Authentication connectedUser
    ) {
        businessService.deleteBusinessByOwner(id, connectedUser);
        return ResponseEntity.noContent().build();
    }
}
