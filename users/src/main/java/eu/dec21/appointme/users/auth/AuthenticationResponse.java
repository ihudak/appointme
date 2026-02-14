package eu.dec21.appointme.users.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@Schema(description = "Authentication response containing JWT Bearer token for subsequent API requests")
public class AuthenticationResponse {
    @Schema(description = "JWT Bearer token (expires after 24 hours)", 
            example = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huLmRvZUBleGFtcGxlLmNvbSIsImlhdCI6MTY0MDk5NTIwMCwiZXhwIjoxNjQxMDgxNjAwfQ.abc123...",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String token;
}
