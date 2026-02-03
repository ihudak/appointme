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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("businesses/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Business Admin", description = "Admin Business API - Requires ADMIN role")
public class AdminBusinessController {

    private final BusinessService businessService;

    @GetMapping
    @Operation(
        summary = "Get all businesses",
        description = "Retrieves all businesses. Set includeInactive=true to see inactive businesses."
    )
    public ResponseEntity<PageResponse<BusinessResponse>> getAllBusinesses(
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size,
            @RequestParam(name = "includeInactive", defaultValue = "false", required = false) boolean includeInactive
    ) {
        return ResponseEntity.ok(businessService.findAllBusinesses(page, size, includeInactive));
    }

    @GetMapping("{id}")
    @Operation(summary = "Get any business by ID", description = "Retrieves any business by its ID (including inactive)")
    public ResponseEntity<BusinessResponse> getBusinessById(@PathVariable Long id) {
        return ResponseEntity.ok(businessService.findByIdAdmin(id));
    }

    @PutMapping("{id}")
    @Operation(summary = "Update any business", description = "Updates any business (for moderation purposes)")
    public ResponseEntity<BusinessResponse> updateBusiness(
            @PathVariable Long id,
            @Valid @RequestBody BusinessRequest request
    ) {
        return ResponseEntity.ok(businessService.updateBusinessByAdmin(id, request));
    }

    @PatchMapping("{id}/active")
    @Operation(summary = "Toggle business active status", description = "Activates or deactivates any business (block/unblock)")
    public ResponseEntity<BusinessResponse> toggleBusinessActive(
            @PathVariable Long id,
            @RequestParam boolean active
    ) {
        return ResponseEntity.ok(businessService.toggleBusinessActiveByAdmin(id, active));
    }

    @DeleteMapping("{id}")
    @Operation(summary = "Delete any business", description = "Permanently deletes any business")
    public ResponseEntity<Void> deleteBusiness(@PathVariable Long id) {
        businessService.deleteBusinessByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
