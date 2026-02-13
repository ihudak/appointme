package eu.dec21.appointme.users.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive tests for AuthRegBaseRequest abstract base class.
 * Tests the base functionality, validation annotations, and behavior
 * that all authentication/registration request DTOs inherit.
 */
@DisplayName("AuthRegBaseRequest Tests")
class AuthRegBaseRequestTest {

    private Validator validator;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        objectMapper = new ObjectMapper();
    }

    /**
     * Concrete implementation of AuthRegBaseRequest for testing.
     * Since AuthRegBaseRequest is abstract, we need a concrete class to instantiate.
     * Uses manual getters/setters and builder pattern since Lombok doesn't work in test inner classes.
     */
    static class TestAuthRequest extends AuthRegBaseRequest {
        // No additional fields - testing only base class behavior
        
        public TestAuthRequest() {
            super();
        }
        
        public TestAuthRequest(String email, String password) {
            super(email, password);
        }
        
        public static TestAuthRequestBuilder builder() {
            return new TestAuthRequestBuilder();
        }
        
        public static class TestAuthRequestBuilder {
            private String email;
            private String password;
            
            public TestAuthRequestBuilder email(String email) {
                this.email = email;
                return this;
            }
            
            public TestAuthRequestBuilder password(String password) {
                this.password = password;
                return this;
            }
            
            public TestAuthRequest build() {
                return new TestAuthRequest(email, password);
            }
        }
    }

    @Nested
    @DisplayName("1. Abstract Class Structure Tests")
    class AbstractClassStructureTests {

        @Test
        @DisplayName("Should be an abstract class")
        void shouldBeAbstractClass() {
            assertThat(AuthRegBaseRequest.class)
                    .withFailMessage("AuthRegBaseRequest should be abstract")
                    .isAbstract();
        }

        @Test
        @DisplayName("Should have email field")
        void shouldHaveEmailField() throws NoSuchFieldException {
            var emailField = AuthRegBaseRequest.class.getDeclaredField("email");
            assertThat(emailField).isNotNull();
            assertThat(emailField.getType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("Should have password field")
        void shouldHavePasswordField() throws NoSuchFieldException {
            var passwordField = AuthRegBaseRequest.class.getDeclaredField("password");
            assertThat(passwordField).isNotNull();
            assertThat(passwordField.getType()).isEqualTo(String.class);
        }

        @Test
        @DisplayName("Should have exactly 2 declared fields")
        void shouldHaveTwoDeclaredFields() {
            var fields = AuthRegBaseRequest.class.getDeclaredFields();
            assertThat(fields).hasSize(2);
        }

        @Test
        @DisplayName("Should have public constructor via Lombok")
        void shouldHavePublicConstructor() {
            var constructors = AuthRegBaseRequest.class.getDeclaredConstructors();
            assertThat(constructors).isNotEmpty();
        }

        @Test
        @DisplayName("Should be extendable by concrete classes")
        void shouldBeExtendableByConcreteClasses() {
            // TestAuthRequest is a concrete implementation
            assertThat(TestAuthRequest.class.getSuperclass())
                    .isEqualTo(AuthRegBaseRequest.class);
        }
    }

    @Nested
    @DisplayName("2. Object Creation Tests")
    class ObjectCreationTests {

        @Test
        @DisplayName("Should create instance via SuperBuilder with all fields")
        void shouldCreateInstanceViaSuperBuilderWithAllFields() {
            var request = TestAuthRequest.builder()
                    .email("test@example.com")
                    .password("SecurePass123!")
                    .build();

            assertThat(request).isNotNull();
            assertThat(request.getEmail()).isEqualTo("test@example.com");
            assertThat(request.getPassword()).isEqualTo("SecurePass123!");
        }

        @Test
        @DisplayName("Should create instance with null email")
        void shouldCreateInstanceWithNullEmail() {
            var request = TestAuthRequest.builder()
                    .email(null)
                    .password("SecurePass123!")
                    .build();

            assertThat(request).isNotNull();
            assertThat(request.getEmail()).isNull();
            assertThat(request.getPassword()).isEqualTo("SecurePass123!");
        }

        @Test
        @DisplayName("Should create instance with null password")
        void shouldCreateInstanceWithNullPassword() {
            var request = TestAuthRequest.builder()
                    .email("test@example.com")
                    .password(null)
                    .build();

            assertThat(request).isNotNull();
            assertThat(request.getEmail()).isEqualTo("test@example.com");
            assertThat(request.getPassword()).isNull();
        }

        @Test
        @DisplayName("Should create instance with both fields null")
        void shouldCreateInstanceWithBothFieldsNull() {
            var request = TestAuthRequest.builder().build();

            assertThat(request).isNotNull();
            assertThat(request.getEmail()).isNull();
            assertThat(request.getPassword()).isNull();
        }

        @Test
        @DisplayName("Should create instance with empty strings")
        void shouldCreateInstanceWithEmptyStrings() {
            var request = TestAuthRequest.builder()
                    .email("")
                    .password("")
                    .build();

            assertThat(request).isNotNull();
            assertThat(request.getEmail()).isEmpty();
            assertThat(request.getPassword()).isEmpty();
        }

        @Test
        @DisplayName("Should create instance with whitespace strings")
        void shouldCreateInstanceWithWhitespaceStrings() {
            var request = TestAuthRequest.builder()
                    .email("   ")
                    .password("   ")
                    .build();

            assertThat(request).isNotNull();
            assertThat(request.getEmail()).isEqualTo("   ");
            assertThat(request.getPassword()).isEqualTo("   ");
        }
    }

    @Nested
    @DisplayName("3. Getter and Setter Tests")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set email via Lombok")
        void shouldGetAndSetEmail() {
            var request = TestAuthRequest.builder().build();
            request.setEmail("new@example.com");

            assertThat(request.getEmail()).isEqualTo("new@example.com");
        }

        @Test
        @DisplayName("Should get and set password via Lombok")
        void shouldGetAndSetPassword() {
            var request = TestAuthRequest.builder().build();
            request.setPassword("NewPassword123!");

            assertThat(request.getPassword()).isEqualTo("NewPassword123!");
        }

        @Test
        @DisplayName("Should allow setting email to null")
        void shouldAllowSettingEmailToNull() {
            var request = TestAuthRequest.builder()
                    .email("test@example.com")
                    .build();
            request.setEmail(null);

            assertThat(request.getEmail()).isNull();
        }

        @Test
        @DisplayName("Should allow setting password to null")
        void shouldAllowSettingPasswordToNull() {
            var request = TestAuthRequest.builder()
                    .password("SecurePass123!")
                    .build();
            request.setPassword(null);

            assertThat(request.getPassword()).isNull();
        }

        @Test
        @DisplayName("Should allow multiple updates to email")
        void shouldAllowMultipleUpdatesToEmail() {
            var request = TestAuthRequest.builder().build();
            request.setEmail("first@example.com");
            request.setEmail("second@example.com");
            request.setEmail("third@example.com");

            assertThat(request.getEmail()).isEqualTo("third@example.com");
        }

        @Test
        @DisplayName("Should allow multiple updates to password")
        void shouldAllowMultipleUpdatesToPassword() {
            var request = TestAuthRequest.builder().build();
            request.setPassword("FirstPass123!");
            request.setPassword("SecondPass123!");
            request.setPassword("ThirdPass123!");

            assertThat(request.getPassword()).isEqualTo("ThirdPass123!");
        }
    }

    @Nested
    @DisplayName("4. SuperBuilder Pattern Tests")
    class SuperBuilderPatternTests {

        @Test
        @DisplayName("Should support method chaining in builder")
        void shouldSupportMethodChainingInBuilder() {
            var request = TestAuthRequest.builder()
                    .email("test@example.com")
                    .password("SecurePass123!")
                    .build();

            assertThat(request.getEmail()).isEqualTo("test@example.com");
            assertThat(request.getPassword()).isEqualTo("SecurePass123!");
        }

        @Test
        @DisplayName("Should allow overriding values in builder")
        void shouldAllowOverridingValuesInBuilder() {
            var request = TestAuthRequest.builder()
                    .email("first@example.com")
                    .email("second@example.com")
                    .password("FirstPass123!")
                    .password("SecondPass123!")
                    .build();

            assertThat(request.getEmail()).isEqualTo("second@example.com");
            assertThat(request.getPassword()).isEqualTo("SecondPass123!");
        }

        @Test
        @DisplayName("Should build multiple independent instances")
        void shouldBuildMultipleIndependentInstances() {
            var request1 = TestAuthRequest.builder()
                    .email("user1@example.com")
                    .password("Password1!")
                    .build();

            var request2 = TestAuthRequest.builder()
                    .email("user2@example.com")
                    .password("Password2!")
                    .build();

            assertThat(request1.getEmail()).isEqualTo("user1@example.com");
            assertThat(request2.getEmail()).isEqualTo("user2@example.com");
            assertThat(request1).isNotSameAs(request2);
        }

        @Test
        @DisplayName("Should support partial field initialization")
        void shouldSupportPartialFieldInitialization() {
            var request = TestAuthRequest.builder()
                    .email("test@example.com")
                    .build();

            assertThat(request.getEmail()).isEqualTo("test@example.com");
            assertThat(request.getPassword()).isNull();
        }
    }

    @Nested
    @DisplayName("5. Email Validation Tests")
    class EmailValidationTests {

        @Test
        @DisplayName("Should accept valid email format")
        void shouldAcceptValidEmailFormat() {
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password("SecurePass123!")
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should reject null email (@NotEmpty)")
        void shouldRejectNullEmail() {
            var request = TestAuthRequest.builder()
                    .email(null)
                    .password("SecurePass123!")
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations)
                    .hasSizeGreaterThanOrEqualTo(1)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Email must not be empty");
        }

        @Test
        @DisplayName("Should reject empty email (@NotEmpty)")
        void shouldRejectEmptyEmail() {
            var request = TestAuthRequest.builder()
                    .email("")
                    .password("SecurePass123!")
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations)
                    .hasSizeGreaterThanOrEqualTo(1)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Email must not be empty");
        }

        @Test
        @DisplayName("Should reject blank email (@NotBlank)")
        void shouldRejectBlankEmail() {
            var request = TestAuthRequest.builder()
                    .email("   ")
                    .password("SecurePass123!")
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations)
                    .hasSizeGreaterThanOrEqualTo(1)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Email must not be blank");
        }

        @Test
        @DisplayName("Should reject invalid email format (@Email)")
        void shouldRejectInvalidEmailFormat() {
            var request = TestAuthRequest.builder()
                    .email("not-an-email")
                    .password("SecurePass123!")
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations)
                    .hasSizeGreaterThanOrEqualTo(1)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Email should be valid");
        }

        @Test
        @DisplayName("Should accept email with plus sign")
        void shouldAcceptEmailWithPlusSign() {
            var request = TestAuthRequest.builder()
                    .email("user+tag@example.com")
                    .password("SecurePass123!")
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should accept email with subdomain")
        void shouldAcceptEmailWithSubdomain() {
            var request = TestAuthRequest.builder()
                    .email("user@mail.example.com")
                    .password("SecurePass123!")
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should accept email with numbers")
        void shouldAcceptEmailWithNumbers() {
            var request = TestAuthRequest.builder()
                    .email("user123@example456.com")
                    .password("SecurePass123!")
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }
    }

    @Nested
    @DisplayName("6. Password Validation Tests - Security Critical")
    class PasswordValidationTests {

        @Test
        @DisplayName("Should accept valid password (8+ chars, mixed case, digit, special)")
        void shouldAcceptValidPassword() {
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password("SecurePass123!")
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("SECURITY: Should reject null password (@NotEmpty) - Prevents BCrypt bug")
        void shouldRejectNullPassword() {
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password(null)
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations)
                    .hasSizeGreaterThanOrEqualTo(1)
                    .extracting(ConstraintViolation::getMessage)
                    .contains("Password must not be empty");
        }

        @Test
        @DisplayName("SECURITY: Should reject empty password (@NotEmpty) - Prevents BCrypt encode/match inconsistency")
        void shouldRejectEmptyPassword() {
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password("")
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);

            var messages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .toList();
            assertThat(messages).contains("Password must not be empty");
        }

        @Test
        @DisplayName("SECURITY: Should reject blank password (@NotBlank)")
        void shouldRejectBlankPassword() {
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password("   ")
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);

            var messages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .toList();
            assertThat(messages).contains("Password must not be blank");
        }

        @Test
        @DisplayName("SECURITY: Should reject password shorter than 8 characters (@Size)")
        void shouldRejectPasswordShorterThan8Characters() {
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password("Short1!")
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);

            var messages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .toList();
            assertThat(messages).contains("Password must be between 8 and 72 characters long");
        }

        @Test
        @DisplayName("SECURITY: Should reject password longer than 72 characters (@Size) - BCrypt limit")
        void shouldRejectPasswordLongerThan72Characters() {
            var longPassword = "A".repeat(50) + "a".repeat(20) + "123!"; // 74 chars
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password(longPassword)
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);

            var messages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .toList();
            assertThat(messages).contains("Password must be between 8 and 72 characters long");
        }

        @Test
        @DisplayName("Should accept password at exactly 8 characters (minimum boundary)")
        void shouldAcceptPasswordAtExactly8Characters() {
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password("Abcd123!") // Exactly 8 chars
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should accept password at exactly 72 characters (maximum boundary)")
        void shouldAcceptPasswordAtExactly72Characters() {
            var password72 = "A".repeat(50) + "a".repeat(18) + "123!"; // Exactly 72 chars
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password(password72)
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("SECURITY: Should reject password without uppercase letter (@Pattern)")
        void shouldRejectPasswordWithoutUppercase() {
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password("lowercase123!")
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);

            var messages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .toList();
            assertThat(messages).contains("Password must contain uppercase, lowercase, digit, special character");
        }

        @Test
        @DisplayName("SECURITY: Should reject password without lowercase letter (@Pattern)")
        void shouldRejectPasswordWithoutLowercase() {
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password("UPPERCASE123!")
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);

            var messages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .toList();
            assertThat(messages).contains("Password must contain uppercase, lowercase, digit, special character");
        }

        @Test
        @DisplayName("SECURITY: Should reject password without digit (@Pattern)")
        void shouldRejectPasswordWithoutDigit() {
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password("NoDigitPass!")
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);

            var messages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .toList();
            assertThat(messages).contains("Password must contain uppercase, lowercase, digit, special character");
        }

        @Test
        @DisplayName("SECURITY: Should reject password without special character (@Pattern)")
        void shouldRejectPasswordWithoutSpecialChar() {
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password("NoSpecial123")
                    .build();

            Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
            assertThat(violations).hasSizeGreaterThanOrEqualTo(1);

            var messages = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .toList();
            assertThat(messages).contains("Password must contain uppercase, lowercase, digit, special character");
        }

        @Test
        @DisplayName("Should accept password with various special characters")
        void shouldAcceptPasswordWithVariousSpecialCharacters() {
            String[] validPasswords = {
                    "SecurePass123!",
                    "SecurePass123@",
                    "SecurePass123#",
                    "SecurePass123$",
                    "SecurePass123%",
                    "SecurePass123^",
                    "SecurePass123&",
                    "SecurePass123*"
            };

            for (String password : validPasswords) {
                var request = TestAuthRequest.builder()
                        .email("user@example.com")
                        .password(password)
                        .build();

                Set<ConstraintViolation<TestAuthRequest>> violations = validator.validate(request);
                assertThat(violations)
                        .withFailMessage("Password '%s' should be valid", password)
                        .isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("7. Edge Cases Tests")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle email with maximum length domain")
        void shouldHandleEmailWithMaxLengthDomain() {
            var longDomain = "a".repeat(50) + ".com";
            var request = TestAuthRequest.builder()
                    .email("user@" + longDomain)
                    .password("SecurePass123!")
                    .build();

            assertThat(request.getEmail()).contains("@" + longDomain);
        }

        @Test
        @DisplayName("Should handle password with Unicode characters")
        void shouldHandlePasswordWithUnicodeCharacters() {
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password("SecurePass123!é")
                    .build();

            assertThat(request.getPassword()).isEqualTo("SecurePass123!é");
        }

        @Test
        @DisplayName("Should handle email with international characters")
        void shouldHandleEmailWithInternationalCharacters() {
            var request = TestAuthRequest.builder()
                    .email("user@exämple.com")
                    .password("SecurePass123!")
                    .build();

            assertThat(request.getEmail()).isEqualTo("user@exämple.com");
        }

        @Test
        @DisplayName("Should handle password with emoji (counts as multiple bytes)")
        void shouldHandlePasswordWithEmoji() {
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password("SecurePass123!😀")
                    .build();

            assertThat(request.getPassword()).contains("😀");
        }

        @Test
        @DisplayName("Should handle email with dots and underscores")
        void shouldHandleEmailWithDotsAndUnderscores() {
            var request = TestAuthRequest.builder()
                    .email("first.last_name@example.co.uk")
                    .password("SecurePass123!")
                    .build();

            assertThat(request.getEmail()).isEqualTo("first.last_name@example.co.uk");
        }

        @Test
        @DisplayName("Should handle password with all printable ASCII characters")
        void shouldHandlePasswordWithAllPrintableAscii() {
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password("ABCabc123!@#$%^&*()")
                    .build();

            assertThat(request.getPassword()).isEqualTo("ABCabc123!@#$%^&*()");
        }

        @Test
        @DisplayName("Should handle very long email address")
        void shouldHandleVeryLongEmail() {
            var longEmail = "a".repeat(50) + "@" + "b".repeat(50) + ".com";
            var request = TestAuthRequest.builder()
                    .email(longEmail)
                    .password("SecurePass123!")
                    .build();

            assertThat(request.getEmail()).hasSize(longEmail.length());
        }

        @Test
        @DisplayName("Should handle password with repeating characters")
        void shouldHandlePasswordWithRepeatingCharacters() {
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password("AAAAAAaaa111!!!")
                    .build();

            assertThat(request.getPassword()).isEqualTo("AAAAAAaaa111!!!");
        }
    }

    @Nested
    @DisplayName("8. Serialization Tests")
    class SerializationTests {

        @Test
        @DisplayName("Should serialize to JSON with all fields")
        void shouldSerializeToJsonWithAllFields() throws Exception {
            var request = TestAuthRequest.builder()
                    .email("test@example.com")
                    .password("SecurePass123!")
                    .build();

            String json = objectMapper.writeValueAsString(request);

            assertThat(json).contains("\"email\":\"test@example.com\"");
            assertThat(json).contains("\"password\":\"SecurePass123!\"");
        }

        @Test
        @DisplayName("Should serialize to JSON with null fields")
        void shouldSerializeToJsonWithNullFields() throws Exception {
            var request = TestAuthRequest.builder().build();

            String json = objectMapper.writeValueAsString(request);

            assertThat(json).contains("\"email\":null");
            assertThat(json).contains("\"password\":null");
        }

        @Test
        @DisplayName("Should deserialize from JSON with all fields")
        void shouldDeserializeFromJsonWithAllFields() throws Exception {
            String json = "{\"email\":\"test@example.com\",\"password\":\"SecurePass123!\"}";

            var request = objectMapper.readValue(json, TestAuthRequest.class);

            assertThat(request).isNotNull();
            assertThat(request.getEmail()).isEqualTo("test@example.com");
            assertThat(request.getPassword()).isEqualTo("SecurePass123!");
        }

        @Test
        @DisplayName("Should deserialize from JSON with null fields")
        void shouldDeserializeFromJsonWithNullFields() throws Exception {
            String json = "{\"email\":null,\"password\":null}";

            var request = objectMapper.readValue(json, TestAuthRequest.class);

            assertThat(request).isNotNull();
            assertThat(request.getEmail()).isNull();
            assertThat(request.getPassword()).isNull();
        }

        @Test
        @DisplayName("Should handle JSON serialization round-trip")
        void shouldHandleJsonSerializationRoundTrip() throws Exception {
            var original = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password("SecurePass123!")
                    .build();

            String json = objectMapper.writeValueAsString(original);
            var deserialized = objectMapper.readValue(json, TestAuthRequest.class);

            assertThat(deserialized.getEmail()).isEqualTo(original.getEmail());
            assertThat(deserialized.getPassword()).isEqualTo(original.getPassword());
        }

        @Test
        @DisplayName("Should handle JSON with special characters")
        void shouldHandleJsonWithSpecialCharacters() throws Exception {
            var request = TestAuthRequest.builder()
                    .email("user+tag@example.com")
                    .password("Pass\"with'quotes123!")
                    .build();

            String json = objectMapper.writeValueAsString(request);
            var deserialized = objectMapper.readValue(json, TestAuthRequest.class);

            assertThat(deserialized.getEmail()).isEqualTo(request.getEmail());
            assertThat(deserialized.getPassword()).isEqualTo(request.getPassword());
        }
    }

    @Nested
    @DisplayName("9. Inheritance Tests")
    class InheritanceTests {

        @Test
        @DisplayName("Should be properly extended by AuthenticationRequest")
        void shouldBeProperlyExtendedByAuthenticationRequest() {
            assertThat(AuthenticationRequest.class.getSuperclass())
                    .isEqualTo(AuthRegBaseRequest.class);
        }

        @Test
        @DisplayName("Should be properly extended by RegistrationRequest")
        void shouldBeProperlyExtendedByRegistrationRequest() {
            assertThat(RegistrationRequest.class.getSuperclass())
                    .isEqualTo(AuthRegBaseRequest.class);
        }

        @Test
        @DisplayName("AuthenticationRequest should inherit email and password fields")
        void authenticationRequestShouldInheritFields() {
            var authRequest = AuthenticationRequest.builder()
                    .email("auth@example.com")
                    .password("AuthPass123!")
                    .build();

            assertThat(authRequest.getEmail()).isEqualTo("auth@example.com");
            assertThat(authRequest.getPassword()).isEqualTo("AuthPass123!");
        }

        @Test
        @DisplayName("RegistrationRequest should inherit email and password fields")
        void registrationRequestShouldInheritFields() {
            var regRequest = RegistrationRequest.builder()
                    .email("reg@example.com")
                    .password("RegPass123!")
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            assertThat(regRequest.getEmail()).isEqualTo("reg@example.com");
            assertThat(regRequest.getPassword()).isEqualTo("RegPass123!");
        }

        @Test
        @DisplayName("Both subclasses should inherit same validation constraints")
        void bothSubclassesShouldInheritSameValidationConstraints() {
            var authRequest = AuthenticationRequest.builder()
                    .email("invalid-email")
                    .password("weak")
                    .build();

            var regRequest = RegistrationRequest.builder()
                    .email("invalid-email")
                    .password("weak")
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            var authViolations = validator.validate(authRequest);
            var regViolations = validator.validate(regRequest);

            // Both should have email and password violations
            assertThat(authViolations).hasSizeGreaterThanOrEqualTo(2);
            assertThat(regViolations).hasSizeGreaterThanOrEqualTo(2);
        }
    }

    @Nested
    @DisplayName("10. Mutability Tests")
    class MutabilityTests {

        @Test
        @DisplayName("Should be mutable - email can be changed after creation")
        void shouldBeMutableEmail() {
            var request = TestAuthRequest.builder()
                    .email("original@example.com")
                    .password("SecurePass123!")
                    .build();

            request.setEmail("modified@example.com");

            assertThat(request.getEmail()).isEqualTo("modified@example.com");
        }

        @Test
        @DisplayName("Should be mutable - password can be changed after creation")
        void shouldBeMutablePassword() {
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password("OriginalPass123!")
                    .build();

            request.setPassword("ModifiedPass123!");

            assertThat(request.getPassword()).isEqualTo("ModifiedPass123!");
        }

        @Test
        @DisplayName("Should allow clearing fields after creation")
        void shouldAllowClearingFieldsAfterCreation() {
            var request = TestAuthRequest.builder()
                    .email("user@example.com")
                    .password("SecurePass123!")
                    .build();

            request.setEmail(null);
            request.setPassword(null);

            assertThat(request.getEmail()).isNull();
            assertThat(request.getPassword()).isNull();
        }
    }
}
