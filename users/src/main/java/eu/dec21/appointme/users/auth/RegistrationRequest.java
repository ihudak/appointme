package eu.dec21.appointme.users.auth;

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
public class RegistrationRequest extends AuthRegBaseRequest {

    @NotEmpty(message = "First name must not be empty")
    @NotBlank(message = "First name must not be blank")
    private String firstName;

    @NotEmpty(message = "Last name must not be empty")
    @NotBlank(message = "Last name must not be blank")
    private String lastName;
}
