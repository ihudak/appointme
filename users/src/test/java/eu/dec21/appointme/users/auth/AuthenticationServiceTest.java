package eu.dec21.appointme.users.auth;

import eu.dec21.appointme.exceptions.ResourceNotFoundException;
import eu.dec21.appointme.exceptions.UserAuthenticationException;
import eu.dec21.appointme.users.email.EmailService;
import eu.dec21.appointme.users.roles.entity.Role;
import eu.dec21.appointme.users.roles.repository.RoleRepository;
import eu.dec21.appointme.users.security.JwtService;
import eu.dec21.appointme.users.tokens.entity.Token;
import eu.dec21.appointme.users.tokens.repository.TokenRepository;
import eu.dec21.appointme.users.users.entity.User;
import eu.dec21.appointme.users.users.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private UserRepository userRepository;
    @Mock private TokenRepository tokenRepository;
    @Mock private EmailService emailService;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private void setConfigFields() throws Exception {
        setField("ACTIVATION_TOKEN_LENGTH", 6);
        setField("ACTIVATION_TOKEN_EXPIRATION_IN_MS", 3600000);
        setField("appName", "AppointMe");
        setField("frontendUrl", "http://localhost:4200");
        setField("activationPath", "verify-account");
    }

    private void setField(String name, Object value) throws Exception {
        Field field = AuthenticationService.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(authenticationService, value);
    }

    // === register ===

    @Test
    void register_success_savesUserAndSendsEmail() throws Exception {
        setConfigFields();
        Role userRole = Role.builder().name("User").build();
        when(roleRepository.findByName("User")).thenReturn(Optional.of(userRole));
        when(passwordEncoder.encode("Pass123!@")).thenReturn("encodedPass");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(tokenRepository.save(any(Token.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .email("john@example.com").password("Pass123!@")
                .build();

        authenticationService.register(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo("john@example.com");
        assertThat(savedUser.getPassword()).isEqualTo("encodedPass");
        assertThat(savedUser.isLocked()).isFalse();
        assertThat(savedUser.isEmailVerified()).isFalse();
        assertThat(savedUser.getRoles()).containsExactly(userRole);

        verify(tokenRepository).save(any(Token.class));
        verify(emailService).sendEmail(anyString(), anyString(), anyString(), any(), anyString(), anyString());
    }

    @Test
    void register_roleNotFound_throwsIllegalStateException() {
        when(roleRepository.findByName("User")).thenReturn(Optional.empty());

        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .email("john@example.com").password("Pass123!@")
                .build();

        assertThatThrownBy(() -> authenticationService.register(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Role User not found");
    }

    // === authenticate ===

    @Test
    void authenticate_success_returnsToken() {
        User user = User.builder()
                .firstName("Jane").lastName("Doe")
                .email("jane@example.com").password("encoded")
                .build();

        var authToken = new UsernamePasswordAuthenticationToken(user, null, List.of());
        when(authenticationManager.authenticate(any())).thenReturn(authToken);
        when(jwtService.generateToken(anyMap(), eq(user))).thenReturn("jwt-token-123");

        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("jane@example.com").password("Pass123!@")
                .build();

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertThat(response.getToken()).isEqualTo("jwt-token-123");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void authenticate_includesFullNameInClaims() {
        User user = User.builder()
                .firstName("Alice").lastName("Smith")
                .email("alice@example.com").password("encoded")
                .build();

        var authToken = new UsernamePasswordAuthenticationToken(user, null, List.of());
        when(authenticationManager.authenticate(any())).thenReturn(authToken);
        when(jwtService.generateToken(anyMap(), eq(user))).thenReturn("token");

        authenticationService.authenticate(
                AuthenticationRequest.builder().email("alice@example.com").password("Pass123!@").build());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<java.util.Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(java.util.Map.class);
        verify(jwtService).generateToken(claimsCaptor.capture(), eq(user));
        assertThat(claimsCaptor.getValue()).containsEntry("fullName", "Smith, Alice");
    }

    // === activateAccount ===

    @Test
    void activateAccount_validToken_activatesUser() {
        User user = User.builder().id(1L).email("test@example.com").emailVerified(false).build();
        Token token = Token.builder()
                .token("abc123")
                .user(user)
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .expiresAt(LocalDateTime.now().plusMinutes(55))
                .build();
        when(tokenRepository.findByToken("abc123")).thenReturn(Optional.of(token));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        authenticationService.activateAccount("abc123");

        assertThat(user.isEmailVerified()).isTrue();
        verify(userRepository).save(user);
        assertThat(token.getValidatedAt()).isNotNull();
        verify(tokenRepository).save(token);
    }

    @Test
    void activateAccount_invalidToken_throwsRuntimeException() {
        when(tokenRepository.findByToken("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.activateAccount("invalid"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid token");
    }

    @Test
    void activateAccount_expiredToken_throwsUserAuthenticationException() throws Exception {
        setConfigFields();
        User user = User.builder().id(1L).email("test@example.com").build();
        Token token = Token.builder()
                .token("expired123")
                .user(user)
                .createdAt(LocalDateTime.now().minusHours(2))
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();
        when(tokenRepository.findByToken("expired123")).thenReturn(Optional.of(token));
        when(tokenRepository.save(any(Token.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> authenticationService.activateAccount("expired123"))
                .isInstanceOf(UserAuthenticationException.class)
                .hasMessageContaining("expired");

        // Verify a new token was sent
        verify(emailService).sendEmail(anyString(), anyString(), anyString(), any(), anyString(), anyString());
    }

    @Test
    void activateAccount_userNotFound_throwsResourceNotFoundException() {
        User user = User.builder().id(999L).email("test@example.com").build();
        Token token = Token.builder()
                .token("abc")
                .user(user)
                .createdAt(LocalDateTime.now().minusMinutes(5))
                .expiresAt(LocalDateTime.now().plusMinutes(55))
                .build();
        when(tokenRepository.findByToken("abc")).thenReturn(Optional.of(token));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.activateAccount("abc"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found");
    }
}
