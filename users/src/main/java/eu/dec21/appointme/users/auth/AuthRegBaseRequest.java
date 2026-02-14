package eu.dec21.appointme.users.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Base class for authentication and registration requests containing email and password")
public abstract class AuthRegBaseRequest {
    @Schema(description = "User email address (must be unique)", example = "john.doe@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotEmpty(message = "Email must not be empty")
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    private String email;

    @Schema(description = "User password (must contain uppercase, lowercase, digit, and special character)", 
            example = "MySecureP@ss123", 
            requiredMode = Schema.RequiredMode.REQUIRED,
            minLength = 8,
            maxLength = 72)
    @NotEmpty(message = "Password must not be empty")
    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, max = 72, message = "Password must be between 8 and 72 characters long")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\w\\s]).+$",
            message = "Password must contain uppercase, lowercase, digit, special character"
    )
    private String password;
}
