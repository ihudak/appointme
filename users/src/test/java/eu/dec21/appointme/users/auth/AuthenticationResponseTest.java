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
 * Comprehensive tests for AuthenticationResponse DTO.
 * Tests object creation, builder patterns, getters/setters, equals/hashCode,
 * toString, serialization, and edge cases.
 */
@DisplayName("AuthenticationResponse Comprehensive Tests")
class AuthenticationResponseTest {

    private static final String VALID_TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

    // ==================== Object Creation Tests ====================

    @Test
    @DisplayName("Should create instance using builder")
    void shouldCreateInstanceUsingBuilder() {
        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(VALID_TOKEN);
    }

    @Test
    @DisplayName("Should create instance with null token using builder")
    void shouldCreateInstanceWithNullTokenUsingBuilder() {
        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(null)
                .build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNull();
    }

    @Test
    @DisplayName("Should create instance without setting token")
    void shouldCreateInstanceWithoutSettingToken() {
        // When
        AuthenticationResponse response = AuthenticationResponse.builder().build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNull();
    }

    @Test
    @DisplayName("Should create empty response")
    void shouldCreateEmptyResponse() {
        // When
        AuthenticationResponse response = AuthenticationResponse.builder().build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isNull();
    }

    @Test
    @DisplayName("Should create instance with empty token")
    void shouldCreateInstanceWithEmptyToken() {
        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("")
                .build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("");
    }

    @Test
    @DisplayName("Should create instance with blank token")
    void shouldCreateInstanceWithBlankToken() {
        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("   ")
                .build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("   ");
    }

    // ==================== Getter/Setter Tests ====================

    @Test
    @DisplayName("Should get and set token")
    void shouldGetAndSetToken() {
        // Given
        AuthenticationResponse response = AuthenticationResponse.builder().build();

        // When
        response.setToken(VALID_TOKEN);

        // Then
        assertThat(response.getToken()).isEqualTo(VALID_TOKEN);
    }

    @Test
    @DisplayName("Should allow setting token to null")
    void shouldAllowSettingTokenToNull() {
        // Given
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();

        // When
        response.setToken(null);

        // Then
        assertThat(response.getToken()).isNull();
    }

    @Test
    @DisplayName("Should allow updating token multiple times")
    void shouldAllowUpdatingTokenMultipleTimes() {
        // Given
        AuthenticationResponse response = AuthenticationResponse.builder().build();

        // When/Then
        response.setToken("first-token");
        assertThat(response.getToken()).isEqualTo("first-token");

        response.setToken("second-token");
        assertThat(response.getToken()).isEqualTo("second-token");

        response.setToken(VALID_TOKEN);
        assertThat(response.getToken()).isEqualTo(VALID_TOKEN);
    }

    @Test
    @DisplayName("Should allow setting token to empty string")
    void shouldAllowSettingTokenToEmptyString() {
        // Given
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();

        // When
        response.setToken("");

        // Then
        assertThat(response.getToken()).isEqualTo("");
    }

    @Test
    @DisplayName("Should allow setting token to blank string")
    void shouldAllowSettingTokenToBlankString() {
        // Given
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();

        // When
        response.setToken("   ");

        // Then
        assertThat(response.getToken()).isEqualTo("   ");
    }

    // ==================== Builder Chaining Tests ====================

    @Test
    @DisplayName("Should support builder method chaining")
    void shouldSupportBuilderMethodChaining() {
        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();

        // Then
        assertThat(response.getToken()).isEqualTo(VALID_TOKEN);
    }

    @Test
    @DisplayName("Should allow builder to override token value")
    void shouldAllowBuilderToOverrideTokenValue() {
        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("first-token")
                .token(VALID_TOKEN)  // Override
                .build();

        // Then
        assertThat(response.getToken()).isEqualTo(VALID_TOKEN);
    }

    @Test
    @DisplayName("Should build response with fluent API")
    void shouldBuildResponseWithFluentApi() {
        // When
        AuthenticationResponse response = AuthenticationResponse
                .builder()
                .token(VALID_TOKEN)
                .build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(VALID_TOKEN);
    }

    // ==================== Equals and HashCode Tests ====================

    @Test
    @DisplayName("Should be equal to itself")
    void shouldBeEqualToItself() {
        // Given
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();

        // Then
        assertThat(response).isEqualTo(response);
        assertThat(response.hashCode()).isEqualTo(response.hashCode());
    }

    @Test
    @DisplayName("Should not override equals (uses Object.equals by default)")
    void shouldNotOverrideEquals() {
        // Given
        AuthenticationResponse response1 = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();
        AuthenticationResponse response2 = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();

        // Then - Without @Data or @EqualsAndHashCode, uses Object.equals (reference equality)
        assertThat(response1).isNotEqualTo(response2); // Different objects
        assertThat(response1).isEqualTo(response1); // Same reference
    }

    @Test
    @DisplayName("Should have different hashCodes for different instances (uses Object.hashCode)")
    void shouldHaveDifferentHashCodesForDifferentInstances() {
        // Given
        AuthenticationResponse response1 = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();
        AuthenticationResponse response2 = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();

        // Then - Without @Data or @EqualsAndHashCode, uses Object.hashCode (identity)
        assertThat(response1.hashCode()).isNotEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("Should not be equal to instance with different token")
    void shouldNotBeEqualToInstanceWithDifferentToken() {
        // Given
        AuthenticationResponse response1 = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();
        AuthenticationResponse response2 = AuthenticationResponse.builder()
                .token("different-token")
                .build();

        // Then
        assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    @DisplayName("Should not be equal to null")
    void shouldNotBeEqualToNull() {
        // Given
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();

        // Then
        assertThat(response).isNotEqualTo(null);
    }

    @Test
    @DisplayName("Should not be equal to different class")
    void shouldNotBeEqualToDifferentClass() {
        // Given
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();
        String differentClass = "Not an AuthenticationResponse";

        // Then
        assertThat(response).isNotEqualTo(differentClass);
    }

    @Test
    @DisplayName("Should use reference equality for null tokens (Object.equals)")
    void shouldUseReferenceEqualityForNullTokens() {
        // Given
        AuthenticationResponse response1 = AuthenticationResponse.builder()
                .token(null)
                .build();
        AuthenticationResponse response2 = AuthenticationResponse.builder()
                .token(null)
                .build();

        // Then - Uses Object.equals (reference equality)
        assertThat(response1).isNotEqualTo(response2); // Different objects
        assertThat(response1).isEqualTo(response1); // Same reference
    }

    // ==================== ToString Tests ====================

    @Test
    @DisplayName("Should have toString from Object class (not customized)")
    void shouldHaveToStringFromObjectClass() {
        // Given
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();

        // When
        String result = response.toString();

        // Then - Without @ToString, uses Object.toString() format: ClassName@hashCode
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
        assertThat(result).contains("AuthenticationResponse@");
        // Does NOT contain field values without @ToString
    }

    @Test
    @DisplayName("Should have non-null toString")
    void shouldHaveNonNullToString() {
        // Given
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();

        // When
        String result = response.toString();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).isNotEmpty();
    }

    @Test
    @DisplayName("Should handle null token in toString")
    void shouldHandleNullTokenInToString() {
        // Given
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(null)
                .build();

        // When/Then
        assertThatNoException().isThrownBy(() -> response.toString());
    }

    // ==================== Edge Cases ====================

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t", "\n", "\r\n"})
    @DisplayName("Should accept empty and whitespace tokens")
    void shouldAcceptEmptyAndWhitespaceTokens(String token) {
        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(token)
                .build();

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo(token);
    }

    @Test
    @DisplayName("Should handle very long token")
    void shouldHandleVeryLongToken() {
        // Given - JWT tokens can be very long
        String longToken = "eyJ".repeat(1000) + ".payload." + "signature".repeat(100);

        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(longToken)
                .build();

        // Then
        assertThat(response.getToken()).isEqualTo(longToken);
    }

    @Test
    @DisplayName("Should handle token with special characters")
    void shouldHandleTokenWithSpecialCharacters() {
        // Given
        String specialToken = "token.with-special_chars!@#$%^&*()";

        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(specialToken)
                .build();

        // Then
        assertThat(response.getToken()).isEqualTo(specialToken);
    }

    @Test
    @DisplayName("Should handle token with Unicode characters")
    void shouldHandleTokenWithUnicodeCharacters() {
        // Given
        String unicodeToken = "tökën-with-üñîçødé";

        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(unicodeToken)
                .build();

        // Then
        assertThat(response.getToken()).isEqualTo(unicodeToken);
    }

    @Test
    @DisplayName("Should handle token with emoji")
    void shouldHandleTokenWithEmoji() {
        // Given
        String emojiToken = "token-🔒-secure-🔐";

        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(emojiToken)
                .build();

        // Then
        assertThat(response.getToken()).isEqualTo(emojiToken);
    }

    @Test
    @DisplayName("Should handle token with line breaks")
    void shouldHandleTokenWithLineBreaks() {
        // Given
        String tokenWithLineBreaks = "line1\nline2\rline3\r\nline4";

        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(tokenWithLineBreaks)
                .build();

        // Then
        assertThat(response.getToken()).isEqualTo(tokenWithLineBreaks);
    }

    @Test
    @DisplayName("Should handle JWT token format")
    void shouldHandleJwtTokenFormat() {
        // Given - Real JWT format: header.payload.signature
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(jwtToken)
                .build();

        // Then
        assertThat(response.getToken()).isEqualTo(jwtToken);
        assertThat(response.getToken()).contains(".");
        assertThat(response.getToken().split("\\.")).hasSize(3); // header.payload.signature
    }

    @Test
    @DisplayName("Should handle Base64 encoded token")
    void shouldHandleBase64EncodedToken() {
        // Given - Base64 characters: A-Z, a-z, 0-9, +, /, =
        String base64Token = "VGhpcyBpcyBhIHRlc3QgdG9rZW4gd2l0aCBCYXNlNjQgZW5jb2Rpbmc=";

        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(base64Token)
                .build();

        // Then
        assertThat(response.getToken()).isEqualTo(base64Token);
    }

    @Test
    @DisplayName("Should handle URL-safe Base64 token")
    void shouldHandleUrlSafeBase64Token() {
        // Given - URL-safe Base64 uses - and _ instead of + and /
        String urlSafeToken = "VGhpcyBpcyBhIHRlc3QgdG9rZW4td2l0aC1VUkwtc2FmZV9CYXNlNjQ";

        // When
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(urlSafeToken)
                .build();

        // Then
        assertThat(response.getToken()).isEqualTo(urlSafeToken);
    }

    // ==================== Serialization Tests ====================

    @Test
    @DisplayName("Should serialize to JSON")
    void shouldSerializeToJson() throws Exception {
        // Given
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();
        ObjectMapper mapper = new ObjectMapper();

        // When
        String json = mapper.writeValueAsString(response);

        // Then
        assertThat(json).isNotNull();
        assertThat(json).contains("token");
        assertThat(json).contains(VALID_TOKEN);
    }

    @Test
    @DisplayName("Should serialize null token to JSON")
    void shouldSerializeNullTokenToJson() throws Exception {
        // Given
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(null)
                .build();
        ObjectMapper mapper = new ObjectMapper();

        // When
        String json = mapper.writeValueAsString(response);

        // Then
        assertThat(json).isNotNull();
        // ObjectMapper default behavior for nulls - may include or exclude null fields
    }

    @Test
    @DisplayName("Should serialize empty token to JSON")
    void shouldSerializeEmptyTokenToJson() throws Exception {
        // Given
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("")
                .build();
        ObjectMapper mapper = new ObjectMapper();

        // When
        String json = mapper.writeValueAsString(response);

        // Then
        assertThat(json).isNotNull();
        assertThat(json).contains("token");
    }

    // Note: Deserialization tests omitted - class uses @Builder without @NoArgsConstructor
    // Jackson requires no-args constructor or custom deserializer for deserialization
    // In practice, AuthenticationResponse is only serialized (sent to client), never deserialized

    // ==================== Immutability Tests ====================

    @Test
    @DisplayName("Should be mutable via setter")
    void shouldBeMutableViaSetter() {
        // Given
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token("initial-token")
                .build();

        // When
        response.setToken("updated-token");

        // Then
        assertThat(response.getToken()).isEqualTo("updated-token");
    }

    @Test
    @DisplayName("Should allow field modification after construction")
    void shouldAllowFieldModificationAfterConstruction() {
        // Given
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();
        String originalToken = response.getToken();

        // When
        response.setToken("new-token");

        // Then
        assertThat(response.getToken()).isNotEqualTo(originalToken);
    }

    // ==================== Type Safety Tests ====================

    @Test
    @DisplayName("Should enforce type safety for token field")
    void shouldEnforceTypeSafetyForTokenField() {
        // Given/When
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();

        // Then - Compile-time type safety
        String token = response.getToken();
        assertThat(token).isInstanceOf(String.class);
    }

    // ==================== Business Logic Tests ====================

    @Test
    @DisplayName("Should represent successful authentication")
    void shouldRepresentSuccessfulAuthentication() {
        // Given/When - Response with token indicates successful authentication
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();

        // Then
        assertThat(response.getToken()).isNotNull();
        assertThat(response.getToken()).isNotEmpty();
    }

    @Test
    @DisplayName("Should indicate failed authentication when token is null")
    void shouldIndicateFailedAuthenticationWhenTokenIsNull() {
        // Given/When - Response with null token could indicate failed authentication
        AuthenticationResponse response = AuthenticationResponse.builder()
                .token(null)
                .build();

        // Then
        assertThat(response.getToken()).isNull();
    }

    @Test
    @DisplayName("Should support creating response for different auth scenarios")
    void shouldSupportCreatingResponseForDifferentAuthScenarios() {
        // Given/When
        AuthenticationResponse successResponse = AuthenticationResponse.builder()
                .token(VALID_TOKEN)
                .build();

        AuthenticationResponse emptyResponse = AuthenticationResponse.builder().build();

        // Then
        assertThat(successResponse.getToken()).isNotNull();
        assertThat(emptyResponse.getToken()).isNull();
    }
}
