package eu.dec21.appointme.categories.categories.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Category response with hierarchical information")
public class CategoryResponse {
    @Schema(description = "Unique category identifier", example = "42")
    private Long id;
    
    @Schema(description = "Category name", example = "Restaurants")
    private String name;
    
    @Schema(description = "Category description", example = "All types of restaurants and dining establishments")
    private String description;
    
    @Schema(description = "Parent category ID (null for root categories)", example = "1")
    private Long parentId;
    
    @Schema(description = "Category image URL", example = "https://storage.appointme.eu/categories/42/icon.jpg")
    private String imageUrl;
    
    @Schema(description = "Whether the category is currently active and visible to public", example = "true")
    private boolean active;
}
