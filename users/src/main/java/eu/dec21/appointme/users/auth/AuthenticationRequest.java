package eu.dec21.appointme.users.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
//@NoArgsConstructor
@Schema(description = "Authentication request with email and password credentials")
public class AuthenticationRequest extends AuthRegBaseRequest {

}
