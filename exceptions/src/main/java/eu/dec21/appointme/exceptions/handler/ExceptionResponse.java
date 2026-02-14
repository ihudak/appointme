package eu.dec21.appointme.exceptions.handler;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@Schema(description = "Standard error response returned by the API for all error scenarios (4xx and 5xx status codes)")
public class ExceptionResponse {

    @Schema(description = "Business-specific error code for client-side error handling", example = "409")
    private Integer businessErrorCode;
    
    @Schema(description = "Human-readable description of the business error", example = "A business with this email already exists")
    private String businessErrorDescription;
    
    @Schema(description = "General error message", example = "Duplicate resource")
    private String error;
    
    @Schema(description = "Set of validation error messages (for 400 Bad Request with validation failures)", 
            example = "[\"Email must be valid\", \"Password must contain uppercase letter\"]")
    private Set<String> validationErrors;
    
    @Schema(description = "Map of field-specific errors (field name → error message)", 
            example = "{\"email\": \"Email already in use\", \"password\": \"Password too weak\"}")
    private Map<String, String> errors;
}
