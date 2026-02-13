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
}
