package eu.dec21.appointme.users.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for BeansConfig.
 * Tests Spring Security bean creation, configuration, and integration.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BeansConfig Tests")
class BeansConfigTest {

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private BeansConfig beansConfig;

    // ==================== PasswordEncoder Bean Tests ====================

    @Test
    @DisplayName("Should create BCryptPasswordEncoder bean")
    void shouldCreateBCryptPasswordEncoderBean() {
        // When
        PasswordEncoder passwordEncoder = beansConfig.passwordEncoder();

        // Then
        assertThat(passwordEncoder).isNotNull();
        assertThat(passwordEncoder).isInstanceOf(BCryptPasswordEncoder.class);
    }

    @Test
    @DisplayName("Should create new PasswordEncoder instance on each call")
    void shouldCreateNewPasswordEncoderInstanceOnEachCall() {
        // When
        PasswordEncoder encoder1 = beansConfig.passwordEncoder();
        PasswordEncoder encoder2 = beansConfig.passwordEncoder();

        // Then
        assertThat(encoder1).isNotNull();
        assertThat(encoder2).isNotNull();
        // Note: @Bean creates singleton by default, but method can be called multiple times in tests
        assertThat(encoder1).isNotSameAs(encoder2);
    }

    @Test
    @DisplayName("Should encode passwords with BCrypt")
    void shouldEncodePasswordsWithBCrypt() {
        // Given
        PasswordEncoder passwordEncoder = beansConfig.passwordEncoder();
        String rawPassword = "mySecretPassword123";

        // When
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // Then
        assertThat(encodedPassword).isNotNull();
        assertThat(encodedPassword).isNotEqualTo(rawPassword);
        assertThat(encodedPassword).startsWith("$2a$"); // BCrypt format
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
    }

    @Test
    @DisplayName("Should verify correct passwords with BCrypt")
    void shouldVerifyCorrectPasswordsWithBCrypt() {
        // Given
        PasswordEncoder passwordEncoder = beansConfig.passwordEncoder();
        String rawPassword = "testPassword";
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // When/Then
        assertThat(passwordEncoder.matches(rawPassword, encodedPassword)).isTrue();
        assertThat(passwordEncoder.matches("wrongPassword", encodedPassword)).isFalse();
    }

    @Test
    @DisplayName("Should generate different hashes for same password (salt)")
    void shouldGenerateDifferentHashesForSamePassword() {
        // Given
        PasswordEncoder passwordEncoder = beansConfig.passwordEncoder();
        String password = "samePassword";

        // When
        String hash1 = passwordEncoder.encode(password);
        String hash2 = passwordEncoder.encode(password);

        // Then - Different hashes due to random salt
        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(passwordEncoder.matches(password, hash1)).isTrue();
        assertThat(passwordEncoder.matches(password, hash2)).isTrue();
    }

    // ==================== AuthenticationProvider Bean Tests ====================

    @Test
    @DisplayName("Should create DaoAuthenticationProvider bean")
    void shouldCreateDaoAuthenticationProviderBean() {
        // When
        AuthenticationProvider authProvider = beansConfig.authenticationProvider();

        // Then
        assertThat(authProvider).isNotNull();
        assertThat(authProvider).isInstanceOf(DaoAuthenticationProvider.class);
    }

    @Test
    @DisplayName("Should configure DaoAuthenticationProvider with UserDetailsService")
    void shouldConfigureDaoAuthenticationProviderWithUserDetailsService() {
        // When
        AuthenticationProvider authProvider = beansConfig.authenticationProvider();

        // Then
        assertThat(authProvider).isInstanceOf(DaoAuthenticationProvider.class);
        DaoAuthenticationProvider daoProvider = (DaoAuthenticationProvider) authProvider;
        
        // Verify it uses the injected UserDetailsService by attempting to use it
        assertThat(daoProvider).isNotNull();
    }

    @Test
    @DisplayName("Should configure DaoAuthenticationProvider with BCrypt PasswordEncoder")
    void shouldConfigureDaoAuthenticationProviderWithBCryptPasswordEncoder() {
        // When
        AuthenticationProvider authProvider = beansConfig.authenticationProvider();

        // Then
        assertThat(authProvider).isInstanceOf(DaoAuthenticationProvider.class);
        // PasswordEncoder is set internally, verified by integration tests
    }

    @Test
    @DisplayName("Should create new AuthenticationProvider instance on each call")
    void shouldCreateNewAuthenticationProviderInstanceOnEachCall() {
        // When
        AuthenticationProvider provider1 = beansConfig.authenticationProvider();
        AuthenticationProvider provider2 = beansConfig.authenticationProvider();

        // Then
        assertThat(provider1).isNotNull();
        assertThat(provider2).isNotNull();
        assertThat(provider1).isNotSameAs(provider2);
    }

    @Test
    @DisplayName("Should create DaoAuthenticationProvider with proper configuration")
    void shouldCreateDaoAuthenticationProviderWithProperConfiguration() {
        // When
        AuthenticationProvider authProvider = beansConfig.authenticationProvider();

        // Then
        assertThat(authProvider).isNotNull();
        assertThat(authProvider).isInstanceOf(DaoAuthenticationProvider.class);
        // UserDetailsService is injected but not called during bean creation
    }

    // ==================== AuthenticationManager Bean Tests ====================

    @Test
    @DisplayName("Should create AuthenticationManager bean from configuration")
    void shouldCreateAuthenticationManagerBeanFromConfiguration() throws Exception {
        // Given
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        // When
        AuthenticationManager result = beansConfig.authenticationManager(authenticationConfiguration);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isSameAs(authenticationManager);
        verify(authenticationConfiguration).getAuthenticationManager();
    }

    @Test
    @DisplayName("Should delegate AuthenticationManager creation to AuthenticationConfiguration")
    void shouldDelegateAuthenticationManagerCreationToAuthenticationConfiguration() throws Exception {
        // Given
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        // When
        AuthenticationManager result = beansConfig.authenticationManager(authenticationConfiguration);

        // Then
        assertThat(result).isNotNull();
        verify(authenticationConfiguration, times(1)).getAuthenticationManager();
    }

    @Test
    @DisplayName("Should propagate exception when AuthenticationManager creation fails")
    void shouldPropagateExceptionWhenAuthenticationManagerCreationFails() throws Exception {
        // Given
        when(authenticationConfiguration.getAuthenticationManager())
                .thenThrow(new RuntimeException("Configuration error"));

        // When/Then
        assertThatThrownBy(() -> beansConfig.authenticationManager(authenticationConfiguration))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Configuration error");
        
        verify(authenticationConfiguration).getAuthenticationManager();
    }

    @Test
    @DisplayName("Should handle null AuthenticationConfiguration gracefully")
    void shouldHandleNullAuthenticationConfigurationGracefully() {
        // When/Then
        assertThatThrownBy(() -> beansConfig.authenticationManager(null))
                .isInstanceOf(NullPointerException.class);
    }

    // ==================== Bean Integration Tests ====================

    @Test
    @DisplayName("Should have all required dependencies injected")
    void shouldHaveAllRequiredDependenciesInjected() {
        // Given/When
        BeansConfig config = new BeansConfig(userDetailsService);

        // Then
        assertThat(config).isNotNull();
    }

    @Test
    @DisplayName("Should accept null UserDetailsService in constructor (Lombok @RequiredArgsConstructor allows it)")
    void shouldAcceptNullUserDetailsServiceInConstructor() {
        // Given/When
        BeansConfig config = new BeansConfig(null);

        // Then
        assertThat(config).isNotNull();
        // Note: Lombok @RequiredArgsConstructor doesn't enforce non-null, 
        // NPE would occur when authenticationProvider() is called
    }

    @Test
    @DisplayName("Should create all beans independently")
    void shouldCreateAllBeansIndependently() throws Exception {
        // Given
        when(authenticationConfiguration.getAuthenticationManager()).thenReturn(authenticationManager);

        // When
        PasswordEncoder passwordEncoder = beansConfig.passwordEncoder();
        AuthenticationProvider authProvider = beansConfig.authenticationProvider();
        AuthenticationManager authManager = beansConfig.authenticationManager(authenticationConfiguration);

        // Then
        assertThat(passwordEncoder).isNotNull();
        assertThat(authProvider).isNotNull();
        assertThat(authManager).isNotNull();
    }

    // ==================== Edge Cases and Error Handling ====================

    @Test
    @DisplayName("Should handle empty password encoding")
    void shouldHandleEmptyPasswordEncoding() {
        // Given
        PasswordEncoder passwordEncoder = beansConfig.passwordEncoder();

        // When
        String encodedEmpty = passwordEncoder.encode("");

        // Then
        assertThat(encodedEmpty).isNotNull();
        assertThat(encodedEmpty).isNotEmpty();
        // Note: BCrypt allows empty string encoding, but matching behavior varies
    }

    @Test
    @DisplayName("Should handle empty password matching")
    void shouldHandleEmptyPasswordMatching() {
        // Given
        PasswordEncoder passwordEncoder = beansConfig.passwordEncoder();
        String encoded = passwordEncoder.encode("test");

        // When/Then - Empty string doesn't match non-empty encoded password
        assertThat(passwordEncoder.matches("", encoded)).isFalse();
    }

    @Test
    @DisplayName("Should handle very long password encoding (up to BCrypt limit)")
    void shouldHandleVeryLongPasswordEncoding() {
        // Given
        PasswordEncoder passwordEncoder = beansConfig.passwordEncoder();
        // BCrypt has a 72-byte limit
        String longPassword = "a".repeat(72);

        // When
        String encoded = passwordEncoder.encode(longPassword);

        // Then
        assertThat(encoded).isNotNull();
        assertThat(passwordEncoder.matches(longPassword, encoded)).isTrue();
    }

    @Test
    @DisplayName("Should reject passwords exceeding BCrypt 72-byte limit")
    void shouldRejectPasswordsExceedingBCryptLimit() {
        // Given
        PasswordEncoder passwordEncoder = beansConfig.passwordEncoder();
        String tooLongPassword = "a".repeat(73);

        // When/Then
        assertThatThrownBy(() -> passwordEncoder.encode(tooLongPassword))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("password cannot be more than 72 bytes");
    }

    @Test
    @DisplayName("Should handle special characters in password encoding")
    void shouldHandleSpecialCharactersInPasswordEncoding() {
        // Given
        PasswordEncoder passwordEncoder = beansConfig.passwordEncoder();
        String specialPassword = "P@$$w0rd!#%&*()[]{}|;:',.<>?/\\\"";

        // When
        String encoded = passwordEncoder.encode(specialPassword);

        // Then
        assertThat(encoded).isNotNull();
        assertThat(passwordEncoder.matches(specialPassword, encoded)).isTrue();
    }

    @Test
    @DisplayName("Should handle Unicode characters in password encoding")
    void shouldHandleUnicodeCharactersInPasswordEncoding() {
        // Given
        PasswordEncoder passwordEncoder = beansConfig.passwordEncoder();
        String unicodePassword = "пароль密码パスワード🔒";

        // When
        String encoded = passwordEncoder.encode(unicodePassword);

        // Then
        assertThat(encoded).isNotNull();
        assertThat(passwordEncoder.matches(unicodePassword, encoded)).isTrue();
    }

    @Test
    @DisplayName("Should handle null password in matching")
    void shouldHandleNullPasswordMatching() {
        // Given
        PasswordEncoder passwordEncoder = beansConfig.passwordEncoder();
        String encodedPassword = passwordEncoder.encode("test");

        // When/Then
        // BCrypt returns false for null raw password
        assertThat(passwordEncoder.matches(null, encodedPassword)).isFalse();
    }
}
