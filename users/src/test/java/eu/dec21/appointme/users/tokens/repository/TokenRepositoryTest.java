package eu.dec21.appointme.users.tokens.repository;

import eu.dec21.appointme.users.tokens.entity.Token;
import eu.dec21.appointme.users.users.entity.User;
import eu.dec21.appointme.users.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DisplayName("TokenRepository Tests")
class TokenRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17.2-alpine")
            .withDatabaseName("appme_users")
            .withUsername("pguser")
            .withPassword("p@ssw0rD!");

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;
    private Token token1;
    private Token token2;
    private Token token3;

    @BeforeEach
    void setUp() {
        tokenRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        user1 = createUser("user1@example.com", "User", "One");
        user2 = createUser("user2@example.com", "User", "Two");

        // Create test tokens
        token1 = createToken("token-123-abc", user1, LocalDateTime.now(), LocalDateTime.now().plusHours(24), null);
        token2 = createToken("token-456-def", user1, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusHours(1), null);
        token3 = createToken("token-789-ghi", user2, LocalDateTime.now(), LocalDateTime.now().plusDays(7), LocalDateTime.now().plusHours(1));
    }

    private User createUser(String email, String firstName, String lastName) {
        return userRepository.save(User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .password("password123")
                .emailVerified(true)
                .locked(false)
                .roles(List.of())
                .groups(List.of())
                .build());
    }

    private Token createToken(String tokenValue, User user, LocalDateTime createdAt, LocalDateTime expiresAt, LocalDateTime validatedAt) {
        return tokenRepository.save(Token.builder()
                .token(tokenValue)
                .user(user)
                .createdAt(createdAt)
                .expiresAt(expiresAt)
                .validatedAt(validatedAt)
                .build());
    }

    // ========== findByToken Tests ==========

    @Test
    @DisplayName("findByToken should return token when token exists")
    void findByToken_shouldReturnToken_whenTokenExists() {
        Optional<Token> result = tokenRepository.findByToken("token-123-abc");

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("token-123-abc");
        assertThat(result.get().getUser().getEmail()).isEqualTo("user1@example.com");
    }

    @Test
    @DisplayName("findByToken should return empty when token does not exist")
    void findByToken_shouldReturnEmpty_whenTokenDoesNotExist() {
        Optional<Token> result = tokenRepository.findByToken("nonexistent-token");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByToken should be case-sensitive")
    void findByToken_shouldBeCaseSensitive() {
        Optional<Token> result = tokenRepository.findByToken("TOKEN-123-ABC");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByToken should handle null token gracefully")
    void findByToken_shouldHandleNullToken() {
        Optional<Token> result = tokenRepository.findByToken(null);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByToken should handle empty string token")
    void findByToken_shouldHandleEmptyStringToken() {
        Optional<Token> result = tokenRepository.findByToken("");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findByToken should return token with user loaded")
    void findByToken_shouldReturnTokenWithUserLoaded() {
        Optional<Token> result = tokenRepository.findByToken("token-123-abc");

        assertThat(result).isPresent();
        Token token = result.get();
        assertThat(token.getUser()).isNotNull();
        assertThat(token.getUser().getEmail()).isEqualTo("user1@example.com");
        assertThat(token.getUser().getFirstName()).isEqualTo("User");
    }

    @Test
    @DisplayName("findByToken should find expired token")
    void findByToken_shouldFindExpiredToken() {
        Optional<Token> result = tokenRepository.findByToken("token-456-def");

        assertThat(result).isPresent();
        assertThat(result.get().getExpiresAt()).isBefore(LocalDateTime.now());
    }

    @Test
    @DisplayName("findByToken should find validated token")
    void findByToken_shouldFindValidatedToken() {
        Optional<Token> result = tokenRepository.findByToken("token-789-ghi");

        assertThat(result).isPresent();
        assertThat(result.get().getValidatedAt()).isNotNull();
    }

    // ========== JPA Standard CRUD Tests ==========

    @Test
    @DisplayName("save should persist new token")
    void save_shouldPersistNewToken() {
        Token newToken = Token.builder()
                .token("new-token-xyz")
                .user(user1)
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        Token saved = tokenRepository.save(newToken);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getToken()).isEqualTo("new-token-xyz");
        assertThat(saved.getUser()).isEqualTo(user1);
    }

    @Test
    @DisplayName("save should update existing token")
    void save_shouldUpdateExistingToken() {
        token1.setValidatedAt(LocalDateTime.now());

        Token updated = tokenRepository.save(token1);

        assertThat(updated.getId()).isEqualTo(token1.getId());
        assertThat(updated.getValidatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findById should return token when ID exists")
    void findById_shouldReturnToken_whenIdExists() {
        Optional<Token> result = tokenRepository.findById(token1.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("token-123-abc");
    }

    @Test
    @DisplayName("findById should return empty when ID does not exist")
    void findById_shouldReturnEmpty_whenIdDoesNotExist() {
        Optional<Token> result = tokenRepository.findById(99999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("findAll should return all tokens")
    void findAll_shouldReturnAllTokens() {
        List<Token> tokens = tokenRepository.findAll();

        assertThat(tokens).hasSize(3);
        assertThat(tokens).extracting(Token::getToken)
                .containsExactlyInAnyOrder("token-123-abc", "token-456-def", "token-789-ghi");
    }

    @Test
    @DisplayName("findAll with pagination should return correct page")
    void findAll_withPagination_shouldReturnCorrectPage() {
        Pageable pageable = PageRequest.of(0, 2, Sort.by("token"));

        Page<Token> page = tokenRepository.findAll(pageable);

        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getTotalPages()).isEqualTo(2);
        assertThat(page.getNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("findAll with pagination should return second page")
    void findAll_withPagination_shouldReturnSecondPage() {
        Pageable pageable = PageRequest.of(1, 2, Sort.by("token"));

        Page<Token> page = tokenRepository.findAll(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isEqualTo(3);
        assertThat(page.getNumber()).isEqualTo(1);
    }

    @Test
    @DisplayName("findAll with sort should return tokens sorted by token")
    void findAll_withSort_shouldReturnTokensSortedByToken() {
        List<Token> tokens = tokenRepository.findAll(Sort.by("token"));

        assertThat(tokens).hasSize(3);
        assertThat(tokens.get(0).getToken()).isEqualTo("token-123-abc");
        assertThat(tokens.get(1).getToken()).isEqualTo("token-456-def");
        assertThat(tokens.get(2).getToken()).isEqualTo("token-789-ghi");
    }

    @Test
    @DisplayName("findAll with sort descending should return tokens in reverse order")
    void findAll_withSortDescending_shouldReturnTokensInReverseOrder() {
        List<Token> tokens = tokenRepository.findAll(Sort.by(Sort.Direction.DESC, "token"));

        assertThat(tokens).hasSize(3);
        assertThat(tokens.get(0).getToken()).isEqualTo("token-789-ghi");
        assertThat(tokens.get(1).getToken()).isEqualTo("token-456-def");
        assertThat(tokens.get(2).getToken()).isEqualTo("token-123-abc");
    }

    @Test
    @DisplayName("count should return total number of tokens")
    void count_shouldReturnTotalNumberOfTokens() {
        long count = tokenRepository.count();

        assertThat(count).isEqualTo(3);
    }

    @Test
    @DisplayName("existsById should return true when token exists")
    void existsById_shouldReturnTrue_whenTokenExists() {
        boolean exists = tokenRepository.existsById(token1.getId());

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsById should return false when token does not exist")
    void existsById_shouldReturnFalse_whenTokenDoesNotExist() {
        boolean exists = tokenRepository.existsById(99999L);

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("deleteById should remove token")
    void deleteById_shouldRemoveToken() {
        Long tokenId = token1.getId();

        tokenRepository.deleteById(tokenId);

        assertThat(tokenRepository.existsById(tokenId)).isFalse();
        assertThat(tokenRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("delete should remove token entity")
    void delete_shouldRemoveTokenEntity() {
        tokenRepository.delete(token1);

        assertThat(tokenRepository.existsById(token1.getId())).isFalse();
        assertThat(tokenRepository.count()).isEqualTo(2);
    }

    @Test
    @DisplayName("deleteAll should remove all tokens")
    void deleteAll_shouldRemoveAllTokens() {
        tokenRepository.deleteAll();

        assertThat(tokenRepository.count()).isEqualTo(0);
    }

    // ========== Edge Cases & Special Scenarios ==========

    @Test
    @DisplayName("should handle token with UUID format")
    void shouldHandleTokenWithUUIDFormat() {
        String uuid = UUID.randomUUID().toString();
        Token uuidToken = createToken(uuid, user1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), null);

        Optional<Token> result = tokenRepository.findByToken(uuid);

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(uuid);
    }

    @Test
    @DisplayName("should handle very long token strings")
    void shouldHandleVeryLongTokenStrings() {
        // Max length is 255 - create a token that's exactly 255 chars
        String longToken = "token-" + "a".repeat(249); // 6 + 249 = 255
        Token longTokenEntity = createToken(longToken, user1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), null);

        Optional<Token> result = tokenRepository.findByToken(longToken);

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(longToken);
        assertThat(result.get().getToken().length()).isEqualTo(255);
    }

    @Test
    @DisplayName("should handle token with special characters")
    void shouldHandleTokenWithSpecialCharacters() {
        String specialToken = "token-!@#$%^&*()_+-=[]{}|;:,.<>?";
        Token specialTokenEntity = createToken(specialToken, user1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), null);

        Optional<Token> result = tokenRepository.findByToken(specialToken);

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(specialToken);
    }

    @Test
    @DisplayName("should handle multiple tokens for same user")
    void shouldHandleMultipleTokensForSameUser() {
        Token extraToken1 = createToken("extra-token-1", user1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), null);
        Token extraToken2 = createToken("extra-token-2", user1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), null);

        List<Token> tokens = tokenRepository.findAll();
        long user1Tokens = tokens.stream()
                .filter(t -> t.getUser().getId().equals(user1.getId()))
                .count();

        assertThat(user1Tokens).isEqualTo(4); // token1, token2, extraToken1, extraToken2
    }

    @Test
    @DisplayName("should handle token without validatedAt")
    void shouldHandleTokenWithoutValidatedAt() {
        Optional<Token> result = tokenRepository.findByToken("token-123-abc");

        assertThat(result).isPresent();
        assertThat(result.get().getValidatedAt()).isNull();
    }

    @Test
    @DisplayName("should handle token with validatedAt")
    void shouldHandleTokenWithValidatedAt() {
        Optional<Token> result = tokenRepository.findByToken("token-789-ghi");

        assertThat(result).isPresent();
        assertThat(result.get().getValidatedAt()).isNotNull();
        assertThat(result.get().getValidatedAt()).isAfter(result.get().getCreatedAt());
    }

    @Test
    @DisplayName("should handle token expiration scenarios")
    void shouldHandleTokenExpirationScenarios() {
        // Create tokens with different expiration states
        Token futureToken = createToken("future-token", user1, LocalDateTime.now(), LocalDateTime.now().plusYears(1), null);
        Token recentlyExpiredToken = createToken("recently-expired", user1, LocalDateTime.now().minusHours(2), LocalDateTime.now().minusHours(1), null);

        assertThat(tokenRepository.findByToken("future-token")).isPresent();
        assertThat(tokenRepository.findByToken("recently-expired")).isPresent();
    }

    @Test
    @DisplayName("findAll on empty repository should return empty list")
    void findAll_onEmptyRepository_shouldReturnEmptyList() {
        tokenRepository.deleteAll();

        List<Token> tokens = tokenRepository.findAll();

        assertThat(tokens).isEmpty();
    }

    @Test
    @DisplayName("findAll with pagination on empty repository should return empty page")
    void findAll_withPagination_onEmptyRepository_shouldReturnEmptyPage() {
        tokenRepository.deleteAll();

        Page<Token> page = tokenRepository.findAll(PageRequest.of(0, 10));

        assertThat(page.getContent()).isEmpty();
        assertThat(page.getTotalElements()).isEqualTo(0);
        assertThat(page.getTotalPages()).isEqualTo(0);
    }

    @Test
    @DisplayName("should maintain referential integrity with user")
    void shouldMaintainReferentialIntegrityWithUser() {
        Token token = tokenRepository.findById(token1.getId()).orElseThrow();

        assertThat(token.getUser()).isNotNull();
        assertThat(token.getUser().getId()).isEqualTo(user1.getId());
    }

    @Test
    @DisplayName("findByToken should find exact matches only")
    void findByToken_shouldFindExactMatchesOnly() {
        createToken("token", user1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), null);
        createToken("token-1", user1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), null);
        createToken("token-12", user1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), null);

        Optional<Token> result = tokenRepository.findByToken("token-1");

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo("token-1");
    }

    @Test
    @DisplayName("should handle token with whitespace")
    void shouldHandleTokenWithWhitespace() {
        String tokenWithSpace = "token with space";
        Token tokenEntity = createToken(tokenWithSpace, user1, LocalDateTime.now(), LocalDateTime.now().plusDays(1), null);

        Optional<Token> result = tokenRepository.findByToken(tokenWithSpace);

        assertThat(result).isPresent();
        assertThat(result.get().getToken()).isEqualTo(tokenWithSpace);
    }

    @Test
    @DisplayName("deleteById should not throw when ID does not exist")
    void deleteById_shouldNotThrow_whenIdDoesNotExist() {
        // Spring Data JPA's deleteById silently succeeds if entity doesn't exist
        tokenRepository.deleteById(99999L);

        // Verify other tokens still exist
        assertThat(tokenRepository.count()).isEqualTo(3);
    }

    @Test
    @DisplayName("should handle sorting by createdAt")
    void shouldHandleSortingByCreatedAt() {
        List<Token> tokens = tokenRepository.findAll(Sort.by("createdAt"));

        assertThat(tokens).hasSize(3);
        // token2 was created yesterday, token1 and token3 today
        assertThat(tokens.get(0).getToken()).isEqualTo("token-456-def");
    }

    @Test
    @DisplayName("should handle sorting by expiresAt")
    void shouldHandleSortingByExpiresAt() {
        List<Token> tokens = tokenRepository.findAll(Sort.by("expiresAt"));

        assertThat(tokens).hasSize(3);
        // token2 expired in the past, token1 expires in 24h, token3 expires in 7 days
        assertThat(tokens.get(0).getToken()).isEqualTo("token-456-def");
        assertThat(tokens.get(1).getToken()).isEqualTo("token-123-abc");
        assertThat(tokens.get(2).getToken()).isEqualTo("token-789-ghi");
    }
}
