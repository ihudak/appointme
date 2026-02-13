package eu.dec21.appointme.users.auth;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive validation tests for AuthenticationRequest.
 * Tests inherited validations from AuthRegBaseRequest including critical security validations
 * for empty/blank/null passwords (addresses BCrypt encoding bug where empty passwords
 * can be encoded but never matched for authentication).
 */
@DisplayName("AuthenticationRequest Validation Tests")
class AuthenticationRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private AuthenticationRequest validRequest() {
        return AuthenticationRequest.builder()
                .email("user@example.com")
                .password("ValidPass1!")
                .build();
    }

    // ==================== Valid Request Tests ====================

    @Test
    @DisplayName("Should accept valid authentication request")
    void shouldAcceptValidAuthenticationRequest() {
        // Given
        AuthenticationRequest request = validRequest();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should accept valid strong password")
    void shouldAcceptValidStrongPassword() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .password("MyStr0ng!Password123")
                .build();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    // ==================== Email Validation Tests ====================

    @Test
    @DisplayName("Should reject null email")
    void shouldRejectNullEmail() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .password("ValidPass1!")
                .build();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("Email must not be empty");
    }

    @Test
    @DisplayName("Should reject empty email")
    void shouldRejectEmptyEmail() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("")
                .password("ValidPass1!")
                .build();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("Email must not be empty");
    }

    @Test
    @DisplayName("Should reject blank email")
    void shouldRejectBlankEmail() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("   ")
                .password("ValidPass1!")
                .build();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("Email must not be blank");
    }

    @ParameterizedTest
    @ValueSource(strings = {"not-an-email", "@nodomain.com", "spaces in@email.com"})
    @DisplayName("Should reject invalid email formats")
    void shouldRejectInvalidEmailFormats(String invalidEmail) {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(invalidEmail)
                .password("ValidPass1!")
                .build();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
    }

    // ==================== SECURITY: Empty/Blank/Null Password Tests ====================
    // These tests prevent the BCrypt bug where empty passwords can be encoded but never authenticated

    @Test
    @DisplayName("SECURITY: Should reject empty password (prevents BCrypt encoding bug)")
    void shouldRejectEmptyPassword() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .password("")
                .build();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("Password must not be empty");
    }

    @Test
    @DisplayName("SECURITY: Should reject blank password (prevents BCrypt encoding bug)")
    void shouldRejectBlankPassword() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .password("   ")
                .build();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("Password must not be blank");
    }

    @Test
    @DisplayName("SECURITY: Should reject null password")
    void shouldRejectNullPassword() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .build();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        // Null triggers @NotEmpty violation
    }

    // ==================== Password Length Validation Tests ====================

    @Test
    @DisplayName("Should reject password shorter than 8 characters")
    void shouldRejectPasswordShorterThan8Characters() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .password("Pass1!")
                .build();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("Password must be between 8 and 72 characters long");
    }

    @Test
    @DisplayName("SECURITY: Should reject password exceeding 72 bytes (BCrypt limit)")
    void shouldRejectPasswordExceeding72Bytes() {
        // Given - BCrypt has 72-byte limit, exceeding causes encoding failure
        String tooLongPassword = "A".repeat(73) + "a1!"; // 73+ chars exceeds BCrypt limit
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .password(tooLongPassword)
                .build();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("Password must be between 8 and 72 characters long");
    }

    @Test
    @DisplayName("Should accept password exactly at 72-byte BCrypt limit")
    void shouldAcceptPasswordExactly72Bytes() {
        // Given - 72 bytes is BCrypt maximum, should be valid
        String maxPassword = "Aa1!" + "a".repeat(68); // 72 chars total, meets all requirements
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .password(maxPassword)
                .build();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    // ==================== Password Complexity Validation Tests ====================

    @ParameterizedTest
    @ValueSource(strings = {
            "nouppercase1!",      // No uppercase
            "NOLOWERCASE1!",      // No lowercase
            "NoDigitsHere!",      // No digit
            "NoSpecial123",       // No special character
            "alllowercase",       // No uppercase, digit, or special
            "12345678"            // Only digits
    })
    @DisplayName("Should reject passwords not meeting complexity requirements")
    void shouldRejectPasswordsNotMeetingComplexityRequirements(String weakPassword) {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .password(weakPassword)
                .build();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isNotEmpty();
    }

    @Test
    @DisplayName("Should accept password with all complexity requirements")
    void shouldAcceptPasswordWithAllComplexityRequirements() {
        // Given - uppercase, lowercase, digit, special character
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .password("MyC0mplex!Pass")
                .build();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    // ==================== Edge Case Tests ====================

    @Test
    @DisplayName("Should accept password with various special characters")
    void shouldAcceptPasswordWithVariousSpecialCharacters() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .password("P@ss#w0rd$123")
                .build();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should accept password with Unicode characters")
    void shouldAcceptPasswordWithUnicodeCharacters() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .password("Pàsswörd1!€")
                .build();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).isEmpty();
    }

    @Test
    @DisplayName("Should reject request with all fields null")
    void shouldRejectRequestWithAllFieldsNull() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().build();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then - Multiple violations per field (@NotEmpty, @NotBlank)
        assertThat(violations).hasSizeGreaterThanOrEqualTo(2); // At least email and password violations
    }

    @Test
    @DisplayName("Should reject request with all fields empty")
    void shouldRejectRequestWithAllFieldsEmpty() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("")
                .password("")
                .build();

        // When
        Set<ConstraintViolation<AuthenticationRequest>> violations = validator.validate(request);

        // Then
        assertThat(violations).hasSizeGreaterThanOrEqualTo(2); // At least email and password violations
    }
}
