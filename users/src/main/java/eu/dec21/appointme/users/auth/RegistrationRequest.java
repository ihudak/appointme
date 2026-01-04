package eu.dec21.appointme.users.auth;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class RegistrationRequest {

    @NotEmpty(message = "First name must not be empty")
    @NotBlank(message = "First name must not be blank")
    private final String firstName;

    @NotEmpty(message = "Last name must not be empty")
    @NotBlank(message = "Last name must not be blank")
    private final String lastName;

    @NotEmpty(message = "Email must not be empty")
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Email should be valid")
    private final String email;

    @NotEmpty(message = "Password must not be empty")
    @NotBlank(message = "Password must not be blank")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private final String password;
}
