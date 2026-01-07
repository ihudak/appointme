package eu.dec21.appointme.users.auth;

import eu.dec21.appointme.users.email.EmailService;
import eu.dec21.appointme.users.roles.repository.RoleRepository;
import eu.dec21.appointme.users.tokens.entity.Token;
import eu.dec21.appointme.users.tokens.repository.TokenRepository;
import eu.dec21.appointme.users.users.entity.User;
import eu.dec21.appointme.users.users.repository.UserRepository;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;

import static eu.dec21.appointme.users.email.EmailTemplateName.VERIFY_EMAIL;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final EmailService emailService;

    @Value("${application.security.activation-token.length}")
    private int ACTIVATION_TOKEN_LENGTH;

    @Value("${application.security.activation-token.expiration-in-ms}")
    private int ACTIVATION_TOKEN_EXPIRATION_IN_MS;

    @Value("${application.name}")
    private String appName;

    @Value("${application.frontend.url}")
    private String frontendUrl;

    @Value("${application.email.activation}")
    private String activationPath;


    public void register(RegistrationRequest request) throws MessagingException {
        var userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new IllegalStateException("Role USER not found"));
        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(List.of(userRole))
                .locked(true)
                .emailVerified(false)
                .build();
        userRepository.save(user);
        sendValidationEmail(user);
    }

    private void sendValidationEmail(User user) throws MessagingException {
        var newToken = generateAndSaveEmailVerificationTokenForUser(user);
        emailService.sendEmail(
                user.getEmail(),
                user.fullName(),
                appName + " - Email Verification",
                VERIFY_EMAIL,
                frontendUrl + "/" + activationPath,
                newToken
        );
    }

    private String generateAndSaveEmailVerificationTokenForUser(User user) {
        // generate token, save it associated with the user, and return the token
        String generatedToken = generateActivationToken(ACTIVATION_TOKEN_LENGTH);
        var token = Token.builder()
                .token(generatedToken)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plus(java.time.Duration.ofMillis(ACTIVATION_TOKEN_EXPIRATION_IN_MS)))
                .user(user)
                .build();
        tokenRepository.save(token);
        return generatedToken;
    }

    private String generateActivationToken(int length) {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder tokenBuilder = new StringBuilder();
        SecureRandom secureRandom = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int index = secureRandom.nextInt(characters.length());
            tokenBuilder.append(characters.charAt(index));
        }
        return tokenBuilder.toString();
    }
}
