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

    // ===== First Name Validation Tests =====

    @Test
    void testFirstName_withValidLength() {
        User user = User.builder()
                .firstName("John")
                .email("test@example.com")
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testFirstName_withMinLength() {
        User user = User.builder()
                .firstName("J")
                .email("test@example.com")
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testFirstName_withMaxLength() {
        String maxLengthName = "A".repeat(50);
        User user = User.builder()
                .firstName(maxLengthName)
                .email("test@example.com")
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testFirstName_exceedsMaxLength() {
        String tooLongName = "A".repeat(51);
        User user = User.builder()
                .firstName(tooLongName)
                .email("test@example.com")
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("firstName") &&
                        v.getMessage().contains("1-50 characters")));
    }

    @Test
    void testFirstName_emptyString() {
        User user = User.builder()
                .firstName("")
                .email("test@example.com")
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("firstName")));
    }

    // ===== Last Name Validation Tests =====

    @Test
    void testLastName_withValidLength() {
        User user = User.builder()
                .lastName("Doe")
                .email("test@example.com")
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLastName_withMinLength() {
        User user = User.builder()
                .lastName("D")
                .email("test@example.com")
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLastName_withMaxLength() {
        String maxLengthName = "D".repeat(50);
        User user = User.builder()
                .lastName(maxLengthName)
                .email("test@example.com")
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testLastName_exceedsMaxLength() {
        String tooLongName = "D".repeat(51);
        User user = User.builder()
                .lastName(tooLongName)
                .email("test@example.com")
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("lastName") &&
                        v.getMessage().contains("1-50 characters")));
    }

    @Test
    void testLastName_emptyString() {
        User user = User.builder()
                .lastName("")
                .email("test@example.com")
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("lastName")));
    }

    // ===== Email Length Validation Tests =====

    @Test
    void testEmail_withMaxLength() {
        // @Email validator typically has internal limit around 254 chars
        // Test with a reasonably long but valid email (under both @Size and @Email limits)
        String localPart = "a".repeat(60); // 60 chars
        String email = localPart + "@example-domain-name.com"; // Total: ~85 chars
        User user = User.builder()
                .email(email)
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testEmail_exceedsMaxLength() {
        String localPart = "a".repeat(246); // Too long
        String email = localPart + "@example.com"; // Total: 258 chars
        User user = User.builder()
                .email(email)
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email") &&
                        v.getMessage().contains("must not exceed 255 characters")));
    }

    // ===== Phone Number Length Validation Tests =====

    @Test
    void testPhoneNumber_withMaxLength() {
        String maxLengthPhone = "+123456789012345"; // 16 chars (15 digits + +)
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .phoneNumber(maxLengthPhone)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testPhoneNumber_exceedsMaxLength() {
        String tooLongPhone = "+12345678901234567890"; // 21 chars
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .phoneNumber(tooLongPhone)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("phoneNumber")));
    }

    // ===== Password Validation Tests =====

    @Test
    void testPassword_withMinLength() {
        User user = User.builder()
                .email("test@example.com")
                .password("pass1234") // Exactly 8 chars
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testPassword_belowMinLength() {
        User user = User.builder()
                .email("test@example.com")
                .password("pass123") // Only 7 chars
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password") &&
                        v.getMessage().contains("8-255 characters")));
    }

    @Test
    void testPassword_withMaxLength() {
        String maxLengthPassword = "p".repeat(255);
        User user = User.builder()
                .email("test@example.com")
                .password(maxLengthPassword)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testPassword_exceedsMaxLength() {
        String tooLongPassword = "p".repeat(256);
        User user = User.builder()
                .email("test@example.com")
                .password(tooLongPassword)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password") &&
                        v.getMessage().contains("8-255 characters")));
    }

    // ===== Image URL Validation Tests =====

    @ParameterizedTest
    @ValueSource(strings = {
            "https://example.com/image.jpg",
            "http://localhost:8080/avatar.png",
            "https://cdn.example.com/users/profile/image123.webp",
            "https://storage.googleapis.com/bucket/user-avatars/avatar.jpg"
    })
    void testImageUrl_withValidUrls(String imageUrl) {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .imageUrl(imageUrl)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "not-a-url",
            "just-text",
            "www.missing-protocol.com/image.jpg",
            "htp://typo-in-protocol.com/image.jpg"
    })
    void testImageUrl_withInvalidUrls(String imageUrl) {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .imageUrl(imageUrl)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("imageUrl") &&
                        v.getMessage().contains("Invalid image URL")));
    }

    @Test
    void testImageUrl_withMaxLength() {
        String maxLengthUrl = "https://example.com/" + "a".repeat(2024); // Total: 2048 chars
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .imageUrl(maxLengthUrl)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testImageUrl_exceedsMaxLength() {
        String tooLongUrl = "https://example.com/" + "a".repeat(2030); // Total: 2049+ chars
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .imageUrl(tooLongUrl)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("imageUrl") &&
                        v.getMessage().contains("must not exceed 2048 characters")));
    }

    @Test
    void testImageUrl_nullIsValid() {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .imageUrl(null)
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    // === UserDetails interface method tests ===

    @Test
    void testIsAccountNonExpired_defaultTrue() {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        assertTrue(user.isAccountNonExpired());
    }

    @Test
    void testIsAccountNonLocked_whenNotLocked() {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .locked(false)
                .build();

        assertTrue(user.isAccountNonLocked());
    }

    @Test
    void testIsAccountNonLocked_whenLocked() {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .locked(true)
                .build();

        assertFalse(user.isAccountNonLocked());
    }

    @Test
    void testIsCredentialsNonExpired_defaultTrue() {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        assertTrue(user.isCredentialsNonExpired());
    }

    @Test
    void testIsEnabled_defaultFalse() {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        assertFalse(user.isEnabled());
    }

    @Test
    void testIsEnabled_whenEmailVerified() {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .emailVerified(true)
                .build();

        assertTrue(user.isEnabled());
    }

    @Test
    void testGetPassword_returnsPassword() {
        User user = User.builder()
                .email("test@example.com")
                .password("hashedPassword123")
                .build();

        assertEquals("hashedPassword123", user.getPassword());
    }

    // === Roles and Groups defaults ===

    @Test
    void testRoles_defaultEmptyList() {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        assertNotNull(user.getRoles());
        assertTrue(user.getRoles().isEmpty());
    }

    @Test
    void testGroups_defaultEmptyList() {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        assertNotNull(user.getGroups());
        assertTrue(user.getGroups().isEmpty());
    }

    // === Boolean field defaults ===

    @Test
    void testEmailVerified_defaultFalse() {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        assertFalse(user.isEmailVerified());
    }

    @Test
    void testLocked_defaultFalse() {
        User user = User.builder()
                .email("test@example.com")
                .password("password123")
                .build();

        assertFalse(user.isLocked());
    }
}
