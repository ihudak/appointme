package eu.dec21.appointme.users.auth;

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
@Tag(name = "Authentication")
public class AuthenticationController {

    private final AuthenticationService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<?> registerUser(@RequestBody @Valid RegistrationRequest request) throws MessagingException {
        log.info("POST /auth/register - Registration request received for email: {}", request.getEmail());
        authService.register(request);
        log.info("POST /auth/register - Registration accepted for email: {}", request.getEmail());
        return ResponseEntity.accepted().build();
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticateUser(@RequestBody @Valid AuthenticationRequest request) {
        log.info("POST /auth/authenticate - Authentication request for email: {}", request.getEmail());
        AuthenticationResponse response = authService.authenticate(request);
        log.info("POST /auth/authenticate - Authentication successful for email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-account")
    public void activateAccount(@RequestParam String token) throws MessagingException {
        log.info("GET /auth/verify-account - Account verification request");
        authService.activateAccount(token);
        log.info("GET /auth/verify-account - Account verified successfully");
    }
}
