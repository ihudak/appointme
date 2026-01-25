package eu.dec21.appointme.businesses.businesses.controller;

import eu.dec21.appointme.businesses.businesses.request.BusinessRequest;
import eu.dec21.appointme.businesses.businesses.response.BusinessResponse;
import eu.dec21.appointme.businesses.businesses.service.BusinessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("businesses")
@RequiredArgsConstructor
@Tag(name = "Businesses", description = "Businesses API")
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping
    @Operation(summary = "Create a new business", description = "Creates a new business with the provided details")
    public ResponseEntity<BusinessResponse> createBusiness(
            @Valid @RequestBody BusinessRequest request,
            Authentication connectedUser
    ) {
        return ResponseEntity.ok(businessService.save(request, connectedUser));
    }

    @GetMapping("{id}")
    @Operation(summary = "Get a business by ID", description = "Retrieves a business by its ID")
    public ResponseEntity<BusinessResponse> getBusinessById(@PathVariable Long id) {
        return ResponseEntity.ok(businessService.findById(id));
    }
}
