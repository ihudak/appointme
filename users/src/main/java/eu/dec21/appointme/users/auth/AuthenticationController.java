package eu.dec21.appointme.users.auth;

import eu.dec21.appointme.exceptions.handler.ExceptionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "User authentication and registration API with email verification")
public class AuthenticationController {

    private final AuthenticationService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account and sends a verification email. The account will be inactive until " +
                    "the user clicks the verification link in the email. Email must be unique in the system."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "202",
                    description = "Registration accepted - verification email sent to the provided address"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body or validation errors (e.g., weak password, invalid email format)",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "User with this email already exists",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            )
    })
    public ResponseEntity<?> registerUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User registration details including name, email, and password",
                    required = true
            )
            @RequestBody @Valid RegistrationRequest request
    ) throws MessagingException {
        log.info("POST /auth/register - Registration request received for email: {}", request.getEmail());
        authService.register(request);
        log.info("POST /auth/register - Registration accepted for email: {}", request.getEmail());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/authenticate")
    @Operation(
            summary = "Authenticate user and get JWT token",
            description = "Authenticates a user with email and password. Returns a JWT Bearer token that must be included " +
                    "in the Authorization header for all protected endpoints. Token expires after 24 hours. " +
                    "Account must be verified via email before authentication is possible."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Authentication successful - JWT token returned",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid credentials or account not verified",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            )
    })
    public ResponseEntity<AuthenticationResponse> authenticateUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "User credentials (email and password)",
                    required = true
            )
            @RequestBody @Valid AuthenticationRequest request
    ) {
        log.info("POST /auth/authenticate - Authentication request for email: {}", request.getEmail());
        AuthenticationResponse response = authService.authenticate(request);
        log.info("POST /auth/authenticate - Authentication successful for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-account")
    @Operation(
            summary = "Verify user account",
            description = "Activates a user account using the verification token sent via email during registration. " +
                    "The token is valid for 15 minutes. After verification, the user can authenticate and use the API."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Account verified successfully - user can now authenticate"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid or expired verification token",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Token not found",
                    content = @Content(schema = @Schema(implementation = ExceptionResponse.class))
            )
    })
    public void activateAccount(
            @Parameter(
                    description = "Verification token from email (format: UUID)",
                    example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                    required = true
            )
            @RequestParam String token
    ) throws MessagingException {
        log.info("GET /auth/verify-account - Account verification request");
        authService.activateAccount(token);
        log.info("GET /auth/verify-account - Account verified successfully");
    }
}
