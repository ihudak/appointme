package eu.dec21.appointme.categories.categories.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for creating or updating a category in the hierarchical category tree")
public record CategoryRequest(
        @Schema(description = "Category ID (only for updates, ignored during creation)", example = "42", accessMode = Schema.AccessMode.READ_ONLY)
        Long id,
        
        @Schema(description = "Category name (required, unique within system)", example = "Restaurants", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Name cannot be null")
        @NotBlank(message = "Name cannot be blank")
        @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
        String name,
        
        @Schema(description = "Category description", example = "All types of restaurants and dining establishments")
        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,
        
        @Schema(description = "Parent category ID (null for root categories, must reference existing category for subcategories)", example = "1")
        @Positive(message = "Parent ID must be positive")
        Long parentId
) {
}
