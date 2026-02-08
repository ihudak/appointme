package eu.dec21.appointme.users.tokens.entity;

import eu.dec21.appointme.users.users.entity.User;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TokenTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testBuilder_withAllFields() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(24);
        LocalDateTime validatedAt = now.plusMinutes(30);

        Token token = Token.builder()
                .token("abc123xyz")
                .createdAt(now)
                .expiresAt(expiresAt)
                .validatedAt(validatedAt)
                .user(user)
                .build();

        assertNotNull(token);
        assertEquals("abc123xyz", token.getToken());
        assertEquals(now, token.getCreatedAt());
        assertEquals(expiresAt, token.getExpiresAt());
        assertEquals(validatedAt, token.getValidatedAt());
        assertEquals(user, token.getUser());
    }

    @Test
    void testBuilder_withMinimalFields() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        Token token = Token.builder()
                .token("token123")
                .user(user)
                .build();

        assertNotNull(token);
        assertEquals("token123", token.getToken());
        assertEquals(user, token.getUser());
        assertNull(token.getCreatedAt());
        assertNull(token.getExpiresAt());
        assertNull(token.getValidatedAt());
    }

    @Test
    void testNoArgsConstructor() {
        Token token = new Token();
        assertNotNull(token);
        assertNull(token.getId());
        assertNull(token.getToken());
        assertNull(token.getCreatedAt());
        assertNull(token.getExpiresAt());
        assertNull(token.getValidatedAt());
        assertNull(token.getUser());
    }

    @Test
    void testAllArgsConstructor() {
        User user = User.builder()
                .email("test@example.com")
                .password("password")
                .build();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(1);
        LocalDateTime validatedAt = now.plusHours(1);

        Token token = new Token(1L, "testtoken", now, expiresAt, validatedAt, user);

        assertNotNull(token);
        assertEquals(1L, token.getId());
        assertEquals("testtoken", token.getToken());
        assertEquals(now, token.getCreatedAt());
        assertEquals(expiresAt, token.getExpiresAt());
        assertEquals(validatedAt, token.getValidatedAt());
        assertEquals(user, token.getUser());
    }

    @Test
    void testSetters() {
        Token token = new Token();
        
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(1);
        LocalDateTime validatedAt = now.plusHours(2);

        token.setId(10L);
        token.setToken("newtoken");
        token.setCreatedAt(now);
        token.setExpiresAt(expiresAt);
        token.setValidatedAt(validatedAt);
        token.setUser(user);

        assertEquals(10L, token.getId());
        assertEquals("newtoken", token.getToken());
        assertEquals(now, token.getCreatedAt());
        assertEquals(expiresAt, token.getExpiresAt());
        assertEquals(validatedAt, token.getValidatedAt());
        assertEquals(user, token.getUser());
    }

    @Test
    void testToken_withUUID() {
        String uuidToken = UUID.randomUUID().toString();
        
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        Token token = Token.builder()
                .token(uuidToken)
                .user(user)
                .build();

        assertEquals(uuidToken, token.getToken());
        assertEquals(36, token.getToken().length()); // UUID string length
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "simple",
            "token-with-dashes",
            "token_with_underscores",
            "TokenWithCamelCase",
            "token123456",
            "UPPERCASE_TOKEN",
            "MixedCase123_Token-Value",
            "a1b2c3d4e5f6g7h8i9j0",
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9", // JWT-like
            "very_long_token_string_with_many_characters_to_test_storage_capacity_123456789"
    })
    void testToken_withVariousFormats(String tokenValue) {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        Token token = Token.builder()
                .token(tokenValue)
                .user(user)
                .build();

        assertEquals(tokenValue, token.getToken());
        Set<ConstraintViolation<Token>> violations = validator.validate(token);
        assertTrue(violations.isEmpty());
    }

    @Test
    void testToken_withNullValue() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        Token token = Token.builder()
                .token(null)
                .user(user)
                .build();

        assertNull(token.getToken());
    }

    @Test
    void testToken_withEmptyString() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        Token token = Token.builder()
                .token("")
                .user(user)
                .build();

        assertEquals("", token.getToken());
    }

    @Test
    void testCreatedAt_currentTime() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        LocalDateTime now = LocalDateTime.now();

        Token token = Token.builder()
                .token("test123")
                .createdAt(now)
                .user(user)
                .build();

        assertEquals(now, token.getCreatedAt());
        assertNotNull(token.getCreatedAt());
    }

    @Test
    void testCreatedAt_pastTime() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        LocalDateTime past = LocalDateTime.now().minusDays(7);

        Token token = Token.builder()
                .token("test123")
                .createdAt(past)
                .user(user)
                .build();

        assertEquals(past, token.getCreatedAt());
        assertTrue(token.getCreatedAt().isBefore(LocalDateTime.now()));
    }

    @Test
    void testExpiresAt_futureTime() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        LocalDateTime future = LocalDateTime.now().plusDays(7);

        Token token = Token.builder()
                .token("test123")
                .expiresAt(future)
                .user(user)
                .build();

        assertEquals(future, token.getExpiresAt());
        assertTrue(token.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void testExpiresAt_24HoursFromNow() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusHours(24);

        Token token = Token.builder()
                .token("test123")
                .createdAt(now)
                .expiresAt(expiresAt)
                .user(user)
                .build();

        assertEquals(24, java.time.Duration.between(token.getCreatedAt(), token.getExpiresAt()).toHours());
    }

    @Test
    void testExpiresAt_shortLivedToken() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusMinutes(15);

        Token token = Token.builder()
                .token("shortlived")
                .createdAt(now)
                .expiresAt(expiresAt)
                .user(user)
                .build();

        assertEquals(15, java.time.Duration.between(token.getCreatedAt(), token.getExpiresAt()).toMinutes());
    }

    @Test
    void testToken_isExpired() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        LocalDateTime past = LocalDateTime.now().minusHours(1);

        Token token = Token.builder()
                .token("expired")
                .expiresAt(past)
                .user(user)
                .build();

        assertTrue(token.getExpiresAt().isBefore(LocalDateTime.now()));
    }

    @Test
    void testToken_isNotExpired() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        LocalDateTime future = LocalDateTime.now().plusHours(1);

        Token token = Token.builder()
                .token("valid")
                .expiresAt(future)
                .user(user)
                .build();

        assertTrue(token.getExpiresAt().isAfter(LocalDateTime.now()));
    }

    @Test
    void testValidatedAt_afterCreation() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        LocalDateTime createdAt = LocalDateTime.now();
        LocalDateTime validatedAt = createdAt.plusMinutes(5);

        Token token = Token.builder()
                .token("test123")
                .createdAt(createdAt)
                .validatedAt(validatedAt)
                .user(user)
                .build();

        assertEquals(validatedAt, token.getValidatedAt());
        assertTrue(token.getValidatedAt().isAfter(token.getCreatedAt()));
    }

    @Test
    void testValidatedAt_null_whenNotValidated() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        Token token = Token.builder()
                .token("test123")
                .user(user)
                .build();

        assertNull(token.getValidatedAt());
    }

    @Test
    void testValidatedAt_canBeSet() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        Token token = Token.builder()
                .token("test123")
                .user(user)
                .build();

        assertNull(token.getValidatedAt());

        LocalDateTime validationTime = LocalDateTime.now();
        token.setValidatedAt(validationTime);

        assertEquals(validationTime, token.getValidatedAt());
    }

    @Test
    void testUser_relationship() {
        User user = User.builder()
                .email("owner@example.com")
                .password("password")
                .build();

        Token token = Token.builder()
                .token("test123")
                .user(user)
                .build();

        assertNotNull(token.getUser());
        assertEquals(user, token.getUser());
        assertEquals("owner@example.com", token.getUser().getEmail());
    }

    @Test
    void testUser_canBeChanged() {
        User user1 = User.builder()
                .email("user1@example.com")
                .password("password")
                .build();

        User user2 = User.builder()
                .email("user2@example.com")
                .password("password")
                .build();

        Token token = Token.builder()
                .token("test123")
                .user(user1)
                .build();

        assertEquals(user1, token.getUser());

        token.setUser(user2);

        assertEquals(user2, token.getUser());
        assertEquals("user2@example.com", token.getUser().getEmail());
    }

    @Test
    void testMultipleTokens_sameUser() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        Token token1 = Token.builder()
                .token("token1")
                .user(user)
                .build();

        Token token2 = Token.builder()
                .token("token2")
                .user(user)
                .build();

        Token token3 = Token.builder()
                .token("token3")
                .user(user)
                .build();

        assertEquals(user, token1.getUser());
        assertEquals(user, token2.getUser());
        assertEquals(user, token3.getUser());
        assertNotEquals(token1.getToken(), token2.getToken());
        assertNotEquals(token2.getToken(), token3.getToken());
    }

    @Test
    void testToken_emailVerificationScenario() {
        User user = User.builder()
                .email("newuser@example.com")
                .password("password")
                .emailVerified(false)
                .build();

        LocalDateTime now = LocalDateTime.now();
        String verificationToken = UUID.randomUUID().toString();

        Token token = Token.builder()
                .token(verificationToken)
                .createdAt(now)
                .expiresAt(now.plusHours(24))
                .user(user)
                .build();

        assertNotNull(token.getToken());
        assertFalse(user.isEmailVerified());
        assertTrue(token.getExpiresAt().isAfter(LocalDateTime.now()));
        assertNull(token.getValidatedAt());
    }

    @Test
    void testToken_passwordResetScenario() {
        User user = User.builder()
                .email("forgot@example.com")
                .password("oldpassword")
                .build();

        LocalDateTime now = LocalDateTime.now();
        String resetToken = UUID.randomUUID().toString();

        Token token = Token.builder()
                .token(resetToken)
                .createdAt(now)
                .expiresAt(now.plusHours(1)) // Short-lived reset token
                .user(user)
                .build();

        assertNotNull(token.getToken());
        assertEquals(1, java.time.Duration.between(token.getCreatedAt(), token.getExpiresAt()).toHours());
        assertNull(token.getValidatedAt());
    }

    @Test
    void testToken_validationWorkflow() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        LocalDateTime createdAt = LocalDateTime.now();
        
        Token token = Token.builder()
                .token("workflow-token")
                .createdAt(createdAt)
                .expiresAt(createdAt.plusDays(1))
                .user(user)
                .build();

        // Before validation
        assertNull(token.getValidatedAt());

        // Simulate validation
        LocalDateTime validationTime = LocalDateTime.now();
        token.setValidatedAt(validationTime);

        // After validation
        assertNotNull(token.getValidatedAt());
        assertTrue(token.getValidatedAt().isAfter(token.getCreatedAt()) || token.getValidatedAt().isEqual(token.getCreatedAt()));
        assertTrue(token.getValidatedAt().isBefore(token.getExpiresAt()) || token.getValidatedAt().isEqual(token.getExpiresAt()));
    }

    @Test
    void testToken_lifecycle() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        // 1. Token created
        LocalDateTime createdAt = LocalDateTime.now();
        Token token = Token.builder()
                .token(UUID.randomUUID().toString())
                .createdAt(createdAt)
                .expiresAt(createdAt.plusHours(24))
                .user(user)
                .build();

        assertNotNull(token.getToken());
        assertNotNull(token.getCreatedAt());
        assertNotNull(token.getExpiresAt());
        assertNull(token.getValidatedAt());

        // 2. Token validated
        LocalDateTime validatedAt = LocalDateTime.now();
        token.setValidatedAt(validatedAt);

        assertNotNull(token.getValidatedAt());
        assertTrue(token.getValidatedAt().isAfter(token.getCreatedAt()) || token.getValidatedAt().isEqual(token.getCreatedAt()));

        // 3. Check if still valid (not expired)
        assertTrue(token.getExpiresAt().isAfter(LocalDateTime.now()) || token.getExpiresAt().isEqual(LocalDateTime.now()));
    }

    @Test
    void testId_autoGenerated() {
        Token token = Token.builder()
                .token("test")
                .build();

        // ID is null before persistence
        assertNull(token.getId());

        // Simulate setting ID after persistence
        token.setId(100L);
        assertEquals(100L, token.getId());
    }

    @Test
    void testToken_differentExpirationPeriods() {
        User user = User.builder()
                .email("user@example.com")
                .password("password")
                .build();

        LocalDateTime now = LocalDateTime.now();

        // 15 minutes
        Token shortToken = Token.builder()
                .token("short")
                .createdAt(now)
                .expiresAt(now.plusMinutes(15))
                .user(user)
                .build();

        // 1 hour
        Token mediumToken = Token.builder()
                .token("medium")
                .createdAt(now)
                .expiresAt(now.plusHours(1))
                .user(user)
                .build();

        // 24 hours
        Token longToken = Token.builder()
                .token("long")
                .createdAt(now)
                .expiresAt(now.plusHours(24))
                .user(user)
                .build();

        // 7 days
        Token extendedToken = Token.builder()
                .token("extended")
                .createdAt(now)
                .expiresAt(now.plusDays(7))
                .user(user)
                .build();

        assertEquals(15, java.time.Duration.between(shortToken.getCreatedAt(), shortToken.getExpiresAt()).toMinutes());
        assertEquals(1, java.time.Duration.between(mediumToken.getCreatedAt(), mediumToken.getExpiresAt()).toHours());
        assertEquals(24, java.time.Duration.between(longToken.getCreatedAt(), longToken.getExpiresAt()).toHours());
        assertEquals(7, java.time.Duration.between(extendedToken.getCreatedAt(), extendedToken.getExpiresAt()).toDays());
    }

    @Test
    void testToken_nullUser_allowedInMemory() {
        // Note: @ManyToOne with nullable=false will fail at persistence, but allowed in memory
        Token token = Token.builder()
                .token("test123")
                .build();

        assertNull(token.getUser());
        assertNotNull(token);
    }
}
