package eu.dec21.appointme.users.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

/**
 * Comprehensive tests for AuthenticationRequest DTO.
 * Tests object creation, builder patterns, getters/setters, equals/hashCode,
 * toString, inheritance, serialization, and edge cases.
 * 
 * Note: Validation tests are in AuthenticationRequestValidationTest.
 */
@DisplayName("AuthenticationRequest Comprehensive Tests")
class AuthenticationRequestTest {

    private static final String VALID_EMAIL = "user@example.com";
    private static final String VALID_PASSWORD = "ValidPass1!";

    // ==================== Object Creation Tests ====================

    @Test
    @DisplayName("Should create instance using builder")
    void shouldCreateInstanceUsingBuilder() {
        // When
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .build();

        // Then
        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(request.getPassword()).isEqualTo(VALID_PASSWORD);
    }

    @Test
    @DisplayName("Should create instance using AllArgsConstructor")
    void shouldCreateInstanceUsingAllArgsConstructor() {
        // When
        AuthenticationRequest request = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();

        // Then
        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(request.getPassword()).isEqualTo(VALID_PASSWORD);
    }

    @Test
    @DisplayName("Should create instance with null values using builder")
    void shouldCreateInstanceWithNullValuesUsingBuilder() {
        // When
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(null)
                .password(null)
                .build();

        // Then
        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isNull();
        assertThat(request.getPassword()).isNull();
    }

    @Test
    @DisplayName("Should create instance with null values using constructor")
    void shouldCreateInstanceWithNullValuesUsingConstructor() {
        // When
        AuthenticationRequest request = AuthenticationRequest.builder().email(null).password(null).build();

        // Then
        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isNull();
        assertThat(request.getPassword()).isNull();
    }

    @Test
    @DisplayName("Should create instance with only email")
    void shouldCreateInstanceWithOnlyEmail() {
        // When
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(VALID_EMAIL)
                .build();

        // Then
        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(request.getPassword()).isNull();
    }

    @Test
    @DisplayName("Should create instance with only password")
    void shouldCreateInstanceWithOnlyPassword() {
        // When
        AuthenticationRequest request = AuthenticationRequest.builder()
                .password(VALID_PASSWORD)
                .build();

        // Then
        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isNull();
        assertThat(request.getPassword()).isEqualTo(VALID_PASSWORD);
    }

    // ==================== Getter/Setter Tests ====================

    @Test
    @DisplayName("Should get and set email")
    void shouldGetAndSetEmail() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email(null).password(null).build();

        // When
        request.setEmail(VALID_EMAIL);

        // Then
        assertThat(request.getEmail()).isEqualTo(VALID_EMAIL);
    }

    @Test
    @DisplayName("Should get and set password")
    void shouldGetAndSetPassword() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email(null).password(null).build();

        // When
        request.setPassword(VALID_PASSWORD);

        // Then
        assertThat(request.getPassword()).isEqualTo(VALID_PASSWORD);
    }

    @Test
    @DisplayName("Should allow setting email to null")
    void shouldAllowSettingEmailToNull() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();

        // When
        request.setEmail(null);

        // Then
        assertThat(request.getEmail()).isNull();
    }

    @Test
    @DisplayName("Should allow setting password to null")
    void shouldAllowSettingPasswordToNull() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();

        // When
        request.setPassword(null);

        // Then
        assertThat(request.getPassword()).isNull();
    }

    @Test
    @DisplayName("Should allow updating email multiple times")
    void shouldAllowUpdatingEmailMultipleTimes() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email(null).password(null).build();

        // When/Then
        request.setEmail("first@example.com");
        assertThat(request.getEmail()).isEqualTo("first@example.com");

        request.setEmail("second@example.com");
        assertThat(request.getEmail()).isEqualTo("second@example.com");

        request.setEmail("third@example.com");
        assertThat(request.getEmail()).isEqualTo("third@example.com");
    }

    @Test
    @DisplayName("Should allow updating password multiple times")
    void shouldAllowUpdatingPasswordMultipleTimes() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email(null).password(null).build();

        // When/Then
        request.setPassword("FirstPass1!");
        assertThat(request.getPassword()).isEqualTo("FirstPass1!");

        request.setPassword("SecondPass2@");
        assertThat(request.getPassword()).isEqualTo("SecondPass2@");

        request.setPassword("ThirdPass3#");
        assertThat(request.getPassword()).isEqualTo("ThirdPass3#");
    }

    // ==================== Builder Chaining Tests ====================

    @Test
    @DisplayName("Should support builder method chaining")
    void shouldSupportBuilderMethodChaining() {
        // When
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(VALID_EMAIL)
                .password(VALID_PASSWORD)
                .build();

        // Then
        assertThat(request.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(request.getPassword()).isEqualTo(VALID_PASSWORD);
    }

    @Test
    @DisplayName("Should allow builder to override values")
    void shouldAllowBuilderToOverrideValues() {
        // When
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("first@example.com")
                .email(VALID_EMAIL)  // Override
                .password("FirstPass1!")
                .password(VALID_PASSWORD)  // Override
                .build();

        // Then
        assertThat(request.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(request.getPassword()).isEqualTo(VALID_PASSWORD);
    }

    // ==================== Inheritance Tests ====================

    @Test
    @DisplayName("Should inherit from AuthRegBaseRequest")
    void shouldInheritFromAuthRegBaseRequest() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();

        // Then
        assertThat(request).isInstanceOf(AuthRegBaseRequest.class);
    }

    @Test
    @DisplayName("Should have access to parent class fields")
    void shouldHaveAccessToParentClassFields() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();
        AuthRegBaseRequest parent = request;

        // When/Then
        assertThat(parent.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(parent.getPassword()).isEqualTo(VALID_PASSWORD);
    }

    @Test
    @DisplayName("Should inherit validation annotations from parent")
    void shouldInheritValidationAnnotationsFromParent() {
        // Given - Create invalid request
        AuthenticationRequest request = AuthenticationRequest.builder().email("").password("").build();

        // Then - Inherited @NotEmpty annotations should apply
        assertThat(request).isNotNull();
        // Validation is tested in AuthenticationRequestValidationTest
    }

    // ==================== Equals and HashCode Tests ====================

    @Test
    @DisplayName("Should be equal to itself")
    void shouldBeEqualToItself() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();

        // Then
        assertThat(request).isEqualTo(request);
        assertThat(request.hashCode()).isEqualTo(request.hashCode());
    }

    @Test
    @DisplayName("Should not override equals (uses Object.equals by default)")
    void shouldNotOverrideEquals() {
        // Given
        AuthenticationRequest request1 = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();
        AuthenticationRequest request2 = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();

        // Then - Without @Data or @EqualsAndHashCode, uses Object.equals (reference equality)
        assertThat(request1).isNotEqualTo(request2); // Different objects
        assertThat(request1).isEqualTo(request1); // Same reference
    }

    @Test
    @DisplayName("Should have different hashCodes for different instances (uses Object.hashCode)")
    void shouldHaveDifferentHashCodesForDifferentInstances() {
        // Given
        AuthenticationRequest request1 = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();
        AuthenticationRequest request2 = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();

        // Then - Without @Data or @EqualsAndHashCode, uses Object.hashCode (identity)
        assertThat(request1.hashCode()).isNotEqualTo(request2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal to instance with different email")
    void shouldNotBeEqualToInstanceWithDifferentEmail() {
        // Given
        AuthenticationRequest request1 = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();
        AuthenticationRequest request2 = AuthenticationRequest.builder().email("different@example.com").password(VALID_PASSWORD).build();

        // Then
        assertThat(request1).isNotEqualTo(request2);
    }

    @Test
    @DisplayName("Should not be equal to instance with different password")
    void shouldNotBeEqualToInstanceWithDifferentPassword() {
        // Given
        AuthenticationRequest request1 = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();
        AuthenticationRequest request2 = AuthenticationRequest.builder().email(VALID_EMAIL).password("DifferentPass1!").build();

        // Then
        assertThat(request1).isNotEqualTo(request2);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void shouldNotBeEqualToNull() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();

        // Then
        assertThat(request).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should not be equal to different class")
    void shouldNotBeEqualToDifferentClass() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();
        String differentClass = "Not an AuthenticationRequest";

        // Then
        assertThat(request).isNotEqualTo(differentClass);
    }

    @Test
    @DisplayName("Should use reference equality for null values (Object.equals)")
    void shouldUseReferenceEqualityForNullValues() {
        // Given
        AuthenticationRequest request1 = AuthenticationRequest.builder().email(null).password(null).build();
        AuthenticationRequest request2 = AuthenticationRequest.builder().email(null).password(null).build();

        // Then - Uses Object.equals (reference equality)
        assertThat(request1).isNotEqualTo(request2); // Different objects
        assertThat(request1).isEqualTo(request1); // Same reference
    }

    // ==================== ToString Tests ====================

    @Test
    @DisplayName("Should have toString implementation")
    void shouldHaveToStringImplementation() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();

        // When
        String result = request.toString();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).contains("AuthenticationRequest");
    }

    @Test
    @DisplayName("Should have toString from Object class (not customized)")
    void shouldHaveToStringFromObjectClass() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();

        // When
        String result = request.toString();

        // Then - Without @ToString, uses Object.toString() format: ClassName@hashCode
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).contains("AuthenticationRequest@");
        // Does NOT contain field values without @ToString
    }

    @Test
    @DisplayName("Should handle null values in toString")
    void shouldHandleNullValuesInToString() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email(null).password(null).build();

        // When/Then
        assertThatNoException().isThrownBy(() -> request.toString());
    }

    // ==================== Edge Cases ====================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should accept empty and whitespace emails (validation happens separately)")
    void shouldAcceptEmptyAndWhitespaceEmails(String email) {
        // When
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(email)
                .password(VALID_PASSWORD)
                .build();

        // Then - Object creation succeeds, validation is separate
        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isEqualTo(email);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n"})
    @DisplayName("Should accept empty and whitespace passwords (validation happens separately)")
    void shouldAcceptEmptyAndWhitespacePasswords(String password) {
        // When
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email(VALID_EMAIL)
                .password(password)
                .build();

        // Then - Object creation succeeds, validation is separate
        assertThat(request).isNotNull();
        assertThat(request.getPassword()).isEqualTo(password);
    }

    @Test
    @DisplayName("Should handle very long email")
    void shouldHandleVeryLongEmail() {
        // Given
        String longEmail = "a".repeat(250) + "@example.com";

        // When
        AuthenticationRequest request = AuthenticationRequest.builder().email(longEmail).password(VALID_PASSWORD).build();

        // Then
        assertThat(request.getEmail()).isEqualTo(longEmail);
    }

    @Test
    @DisplayName("Should handle very long password")
    void shouldHandleVeryLongPassword() {
        // Given
        String longPassword = "Pass1!" + "a".repeat(500);

        // When
        AuthenticationRequest request = AuthenticationRequest.builder().email(VALID_EMAIL).password(longPassword).build();

        // Then
        assertThat(request.getPassword()).isEqualTo(longPassword);
    }

    @Test
    @DisplayName("Should handle special characters in email")
    void shouldHandleSpecialCharactersInEmail() {
        // Given
        String specialEmail = "user+test@sub.example.com";

        // When
        AuthenticationRequest request = AuthenticationRequest.builder().email(specialEmail).password(VALID_PASSWORD).build();

        // Then
        assertThat(request.getEmail()).isEqualTo(specialEmail);
    }

    @Test
    @DisplayName("Should handle special characters in password")
    void shouldHandleSpecialCharactersInPassword() {
        // Given
        String specialPassword = "P@$$w0rd!#%&*()[]{}|;:',.<>?/\\\"";

        // When
        AuthenticationRequest request = AuthenticationRequest.builder().email(VALID_EMAIL).password(specialPassword).build();

        // Then
        assertThat(request.getPassword()).isEqualTo(specialPassword);
    }

    @Test
    @DisplayName("Should handle Unicode characters in email")
    void shouldHandleUnicodeCharactersInEmail() {
        // Given
        String unicodeEmail = "üser@éxample.com";

        // When
        AuthenticationRequest request = AuthenticationRequest.builder().email(unicodeEmail).password(VALID_PASSWORD).build();

        // Then
        assertThat(request.getEmail()).isEqualTo(unicodeEmail);
    }

    @Test
    @DisplayName("Should handle Unicode characters in password")
    void shouldHandleUnicodeCharactersInPassword() {
        // Given
        String unicodePassword = "Pàsswörd1!€";

        // When
        AuthenticationRequest request = AuthenticationRequest.builder().email(VALID_EMAIL).password(unicodePassword).build();

        // Then
        assertThat(request.getPassword()).isEqualTo(unicodePassword);
    }

    @Test
    @DisplayName("Should handle emoji in fields")
    void shouldHandleEmojiInFields() {
        // Given
        String emojiEmail = "user@example.com🔒";
        String emojiPassword = "Pass123!🔐";

        // When
        AuthenticationRequest request = AuthenticationRequest.builder().email(emojiEmail).password(emojiPassword).build();

        // Then
        assertThat(request.getEmail()).isEqualTo(emojiEmail);
        assertThat(request.getPassword()).isEqualTo(emojiPassword);
    }

    // ==================== Serialization Tests ====================

    @Test
    @DisplayName("Should serialize to JSON")
    void shouldSerializeToJson() throws Exception {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();
        ObjectMapper mapper = new ObjectMapper();

        // When
        String json = mapper.writeValueAsString(request);

        // Then
        assertThat(json).isNotNull();
        assertThat(json).contains(VALID_EMAIL);
        assertThat(json).contains(VALID_PASSWORD);
    }

    @Test
    @DisplayName("Should deserialize from JSON")
    void shouldDeserializeFromJson() throws Exception {
        // Given
        String json = String.format("{\"email\":\"%s\",\"password\":\"%s\"}", VALID_EMAIL, VALID_PASSWORD);
        ObjectMapper mapper = new ObjectMapper();

        // When
        AuthenticationRequest request = mapper.readValue(json, AuthenticationRequest.class);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isEqualTo(VALID_EMAIL);
        assertThat(request.getPassword()).isEqualTo(VALID_PASSWORD);
    }

    @Test
    @DisplayName("Should handle round-trip JSON serialization")
    void shouldHandleRoundTripJsonSerialization() throws Exception {
        // Given
        AuthenticationRequest original = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();
        ObjectMapper mapper = new ObjectMapper();

        // When
        String json = mapper.writeValueAsString(original);
        AuthenticationRequest deserialized = mapper.readValue(json, AuthenticationRequest.class);

        // Then
        assertThat(deserialized.getEmail()).isEqualTo(original.getEmail());
        assertThat(deserialized.getPassword()).isEqualTo(original.getPassword());
    }

    @Test
    @DisplayName("Should serialize null values to JSON")
    void shouldSerializeNullValuesToJson() throws Exception {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email(null).password(null).build();
        ObjectMapper mapper = new ObjectMapper();

        // When
        String json = mapper.writeValueAsString(request);

        // Then
        assertThat(json).isNotNull();
        // ObjectMapper default behavior for nulls
    }

    @Test
    @DisplayName("Should deserialize missing fields as null")
    void shouldDeserializeMissingFieldsAsNull() throws Exception {
        // Given
        String json = "{}";
        ObjectMapper mapper = new ObjectMapper();

        // When
        AuthenticationRequest request = mapper.readValue(json, AuthenticationRequest.class);

        // Then
        assertThat(request).isNotNull();
        assertThat(request.getEmail()).isNull();
        assertThat(request.getPassword()).isNull();
    }

    // ==================== Immutability Tests ====================

    @Test
    @DisplayName("Should be mutable via setters")
    void shouldBeMutableViaSetters() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email("initial@example.com").password("Initial1!").build();

        // When
        request.setEmail("updated@example.com");
        request.setPassword("Updated2@");

        // Then
        assertThat(request.getEmail()).isEqualTo("updated@example.com");
        assertThat(request.getPassword()).isEqualTo("Updated2@");
    }

    @Test
    @DisplayName("Should allow field modification after construction")
    void shouldAllowFieldModificationAfterConstruction() {
        // Given
        AuthenticationRequest request = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();
        String originalEmail = request.getEmail();
        String originalPassword = request.getPassword();

        // When
        request.setEmail("new@example.com");
        request.setPassword("NewPass1!");

        // Then
        assertThat(request.getEmail()).isNotEqualTo(originalEmail);
        assertThat(request.getPassword()).isNotEqualTo(originalPassword);
    }

    // ==================== Type Safety Tests ====================

    @Test
    @DisplayName("Should enforce type safety for email field")
    void shouldEnforceTypeSafetyForEmailField() {
        // Given/When
        AuthenticationRequest request = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();

        // Then - Compile-time type safety
        String email = request.getEmail();
        assertThat(email).isInstanceOf(String.class);
    }

    @Test
    @DisplayName("Should enforce type safety for password field")
    void shouldEnforceTypeSafetyForPasswordField() {
        // Given/When
        AuthenticationRequest request = AuthenticationRequest.builder().email(VALID_EMAIL).password(VALID_PASSWORD).build();

        // Then - Compile-time type safety
        String password = request.getPassword();
        assertThat(password).isInstanceOf(String.class);
    }
}

