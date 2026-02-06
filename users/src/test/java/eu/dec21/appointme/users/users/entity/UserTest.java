package eu.dec21.appointme.users.users.entity;

import eu.dec21.appointme.users.roles.entity.Role;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testFullName_withBothNames() {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        assertEquals("Doe, John", user.fullName());
    }

    @Test
    void testFullName_withOnlyLastName() {
        User user = User.builder()
                .lastName("Doe")
                .build();

        assertEquals("Doe", user.fullName());
    }

    @Test
    void testFullName_withOnlyFirstName() {
        User user = User.builder()
                .firstName("John")
                .build();

        assertEquals("John", user.fullName());
    }

    @Test
    void testFullName_withBothNull() {
        User user = User.builder().build();

        assertEquals("", user.fullName());
    }

    @Test
    void testFullName_withWhitespaceOnly() {
        User user = User.builder()
                .firstName("   ")
                .lastName("  ")
                .build();

        assertEquals("", user.fullName());
    }

    @Test
    void testFullName_withTrailingWhitespace() {
        User user = User.builder()
                .firstName("  John  ")
                .lastName("  Doe  ")
                .build();

        assertEquals("Doe, John", user.fullName());
    }

    @Test
    void testFullNameReverse_withBothNames() {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .build();

        assertEquals("John Doe", user.fullNameReverse());
    }

    @Test
    void testFullNameReverse_withOnlyFirstName() {
        User user = User.builder()
                .firstName("John")
                .build();

        assertEquals("John", user.fullNameReverse());
    }

    @Test
    void testFullNameReverse_withOnlyLastName() {
        User user = User.builder()
                .lastName("Doe")
                .build();

        assertEquals("Doe", user.fullNameReverse());
    }

    @Test
    void testFullNameReverse_withBothNull() {
        User user = User.builder().build();

        assertEquals("", user.fullNameReverse());
    }

    @Test
    void testFullNameReverse_withWhitespaceOnly() {
        User user = User.builder()
                .firstName("   ")
                .lastName("  ")
                .build();

        assertEquals("", user.fullNameReverse());
    }

    @Test
    void testFullNameReverse_withTrailingWhitespace() {
        User user = User.builder()
                .firstName("  John  ")
                .lastName("  Doe  ")
                .build();

        assertEquals("John Doe", user.fullNameReverse());
    }

    @Test
    void testGetUsername_returnsEmail() {
        User user = User.builder()
                .email("test@example.com")
                .build();

        assertEquals("test@example.com", user.getUsername());
    }

    @Test
    void testGetName_returnsEmail() {
        User user = User.builder()
                .email("test@example.com")
                .build();

        assertEquals("test@example.com", user.getName());
    }

    @Test
    void testGetAuthorities_mapsRolesCorrectly() {
        Role role1 = mock(Role.class);
        Role role2 = mock(Role.class);
        when(role1.getName()).thenReturn("ROLE_USER");
        when(role2.getName()).thenReturn("ROLE_ADMIN");

        User user = User.builder()
                .email("test@example.com")
                .roles(List.of(role1, role2))
                .build();

        var authorities = user.getAuthorities();
        List<String> authorityNames = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        assertEquals(2, authorityNames.size());
        assertTrue(authorityNames.contains("ROLE_USER"));
        assertTrue(authorityNames.contains("ROLE_ADMIN"));
    }

    @Test
    void testGetAuthorities_withEmptyRoles() {
        User user = User.builder()
                .email("test@example.com")
                .roles(List.of())
                .build();

        var authorities = user.getAuthorities();

        assertTrue(authorities.isEmpty());
    }

    @Test
    void testGetAuthorities_withNullRoles() {
        User user = User.builder()
                .email("test@example.com")
                .roles(null)
                .build();

        var authorities = user.getAuthorities();

        assertTrue(authorities.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "user@example.com",
            "test.user@example.com",
            "test+tag@example.co.uk",
            "user123@sub.domain.com"
    })
    void testEmailValidation_validEmails(String email) {
        User user = User.builder()
                .email(email)
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Valid email should not have violations: " + email);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "plaintext",
            "@nodomain",
            "missing@",
            "spaces in@email.com",
            "user@",
            "@domain.com",
            "user domain@example.com"
    })
    void testEmailValidation_invalidEmails(String email) {
        User user = User.builder()
                .email(email)
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Invalid email should have violations: " + email);
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    void testEmailValidation_blankEmails(String email) {
        User user = User.builder()
                .email(email)
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "+4915112345678",
            "+12125551234",
            "+441234567890",
            "+861234567890123",
            "+919876543210"
    })
    void testPhoneNumberValidation_validE164(String phoneNumber) {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .phoneNumber(phoneNumber)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Valid E.164 phone should not have violations: " + phoneNumber);
    }

    @Test
    void testPhoneNumberValidation_nullIsValid() {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .phoneNumber(null)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertTrue(violations.isEmpty(), "Null phone number should be valid (optional field)");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "123456",
            "+",
            "+123abc",
            "001234567",
            "4915112345678",
            "+0123456789",
            "++4915112345678",
            "+ 4915112345678"
    })
    void testPhoneNumberValidation_invalidE164(String phoneNumber) {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .phoneNumber(phoneNumber)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);

        assertFalse(violations.isEmpty(), "Invalid E.164 phone should have violations: " + phoneNumber);
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber")));
    }

    @Test
    void testDefaultValues() {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        assertFalse(user.isEmailVerified(), "emailVerified should default to false");
        assertFalse(user.isLocked(), "locked should default to false");
    }

    @Test
    void testBuilder_withAllFields() {
        User user = User.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .phoneNumber("+12125551234")
                .password("securePassword")
                .imageUrl("https://example.com/image.jpg")
                .emailVerified(true)
                .locked(false)
                .build();

        assertEquals("John", user.getFirstName());
        assertEquals("Doe", user.getLastName());
        assertEquals("john.doe@example.com", user.getEmail());
        assertEquals("+12125551234", user.getPhoneNumber());
        assertEquals("securePassword", user.getPassword());
        assertEquals("https://example.com/image.jpg", user.getImageUrl());
        assertTrue(user.isEmailVerified());
        assertFalse(user.isLocked());
    }
}
