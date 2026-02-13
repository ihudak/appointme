package eu.dec21.appointme.users.auth;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationRequestValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private RegistrationRequest validRequest() {
        return RegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .email("john@example.com").password("Pass123!@")
                .build();
    }

    @Test
    void validRequest_noViolations() {
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(validRequest());
        assertThat(violations).isEmpty();
    }

    @Test
    void blankFirstName_hasViolation() {
        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("").lastName("Doe")
                .email("john@example.com").password("Pass123!@")
                .build();
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void blankLastName_hasViolation() {
        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John").lastName("")
                .email("john@example.com").password("Pass123!@")
                .build();
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void nullEmail_hasViolation() {
        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .password("Pass123!@")
                .build();
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void invalidEmail_hasViolation() {
        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .email("not-an-email").password("Pass123!@")
                .build();
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"short1!", "nouppercase1!", "NOLOWERCASE1!", "NoDigit!@", "NoSpecial1a"})
    void invalidPassword_hasViolation(String password) {
        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .email("john@example.com").password(password)
                .build();
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void passwordTooShort_hasViolation() {
        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .email("john@example.com").password("P1!a")
                .build();
        assertThat(validator.validate(request)).isNotEmpty();
    }

    @Test
    void validStrongPassword_noViolation() {
        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .email("john@example.com").password("MyStr0ng!Pass")
                .build();
        assertThat(validator.validate(request)).isEmpty();
    }

    // ==================== SECURITY: Empty/Blank/Null Password Tests ====================

    @Test
    void emptyPassword_hasViolation() {
        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .email("john@example.com").password("")
                .build();
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("Password must not be empty");
    }

    @Test
    void blankPassword_hasViolation() {
        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .email("john@example.com").password("   ")
                .build();
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("Password must not be blank");
    }

    @Test
    void nullPassword_hasViolation() {
        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .email("john@example.com")
                .build();
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        // Null triggers @NotEmpty violation
    }

    @Test
    void passwordExceeding72Bytes_hasViolation() {
        // BCrypt has 72-byte limit - this is critical for security
        String tooLongPassword = "A".repeat(73) + "a1!"; // 73 chars exceeds BCrypt limit
        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .email("john@example.com").password(tooLongPassword)
                .build();
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertThat(violations).isNotEmpty();
        assertThat(violations).extracting(ConstraintViolation::getMessage)
                .contains("Password must be between 8 and 72 characters long");
    }

    @Test
    void passwordExactly72Bytes_noViolation() {
        // 72 bytes is the BCrypt limit - should be valid
        String maxPassword = "Aa1!" + "a".repeat(68); // 72 chars total, meets all requirements
        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .email("john@example.com").password(maxPassword)
                .build();
        Set<ConstraintViolation<RegistrationRequest>> violations = validator.validate(request);
        assertThat(violations).isEmpty();
    }
}
