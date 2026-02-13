package eu.dec21.appointme.users.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import javax.crypto.SecretKey;
import java.lang.reflect.Field;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private JwtService jwtService;

    // A valid 256-bit base64-encoded secret key for HS256
    private static final String SECRET_KEY = Base64.getEncoder().encodeToString(
            "ThisIsAVerySecretKeyForTesting12".getBytes());
    private static final long JWT_EXPIRATION = 3600000L; // 1 hour

    @BeforeEach
    void setUp() throws Exception {
        jwtService = new JwtService();
        setField(jwtService, "secretKey", SECRET_KEY);
        setField(jwtService, "jwtExpiration", JWT_EXPIRATION);
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private UserDetails createUser(String email, String... roles) {
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new).toList();
        return new User(email, "password", authorities);
    }

    // === generateToken ===

    @Test
    void generateToken_withUserDetails_returnsNonEmptyToken() {
        UserDetails user = createUser("test@example.com", "ROLE_USER");
        String token = jwtService.generateToken(user);
        assertThat(token).isNotBlank();
    }

    @Test
    void generateToken_withExtraClaims_includesClaims() {
        UserDetails user = createUser("test@example.com");
        Map<String, Object> claims = new HashMap<>();
        claims.put("fullName", "John Doe");

        String token = jwtService.generateToken(claims, user);
        assertThat(token).isNotBlank();
    }

    // === extractUsername ===

    @Test
    void extractUsername_returnsCorrectEmail() {
        UserDetails user = createUser("test@example.com");
        String token = jwtService.generateToken(user);

        String username = jwtService.extractUsername(token);
        assertThat(username).isEqualTo("test@example.com");
    }

    // === extractClaim ===

    @Test
    void extractClaim_subject_returnsUsername() {
        UserDetails user = createUser("user@test.com");
        String token = jwtService.generateToken(user);

        String subject = jwtService.extractClaim(token, claims -> claims.getSubject());
        assertThat(subject).isEqualTo("user@test.com");
    }

    @Test
    void extractClaim_expiration_isInFuture() {
        UserDetails user = createUser("user@test.com");
        String token = jwtService.generateToken(user);

        Date expiration = jwtService.extractClaim(token, claims -> claims.getExpiration());
        assertThat(expiration).isAfter(new Date());
    }

    @Test
    void extractClaim_issuedAt_isNotNull() {
        UserDetails user = createUser("user@test.com");
        String token = jwtService.generateToken(user);

        Date issuedAt = jwtService.extractClaim(token, claims -> claims.getIssuedAt());
        assertThat(issuedAt).isNotNull();
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateToken_includesAuthoritiesInClaim() {
        UserDetails user = createUser("admin@test.com", "ROLE_ADMIN", "ROLE_USER");
        String token = jwtService.generateToken(user);

        List<String> authorities = jwtService.extractClaim(token,
                claims -> claims.get("authorities", List.class));
        assertThat(authorities).containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateToken_noAuthorities_emptyList() {
        UserDetails user = createUser("user@test.com");
        String token = jwtService.generateToken(user);

        List<String> authorities = jwtService.extractClaim(token,
                claims -> claims.get("authorities", List.class));
        assertThat(authorities).isEmpty();
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateToken_withExtraClaims_extractableFromToken() {
        UserDetails user = createUser("user@test.com");
        Map<String, Object> claims = new HashMap<>();
        claims.put("fullName", "Jane Doe");
        String token = jwtService.generateToken(claims, user);

        String fullName = jwtService.extractClaim(token, c -> c.get("fullName", String.class));
        assertThat(fullName).isEqualTo("Jane Doe");
    }

    // === isTokenValid ===

    @Test
    void isTokenValid_validToken_returnsTrue() {
        UserDetails user = createUser("test@example.com");
        String token = jwtService.generateToken(user);
        assertThat(jwtService.isTokenValid(token, user)).isTrue();
    }

    @Test
    void isTokenValid_differentUser_returnsFalse() {
        UserDetails user1 = createUser("user1@example.com");
        UserDetails user2 = createUser("user2@example.com");
        String token = jwtService.generateToken(user1);
        assertThat(jwtService.isTokenValid(token, user2)).isFalse();
    }

    @Test
    void isTokenValid_caseInsensitiveUsername() {
        UserDetails user = createUser("Test@Example.com");
        String token = jwtService.generateToken(user);

        UserDetails sameUserDifferentCase = createUser("test@example.com");
        assertThat(jwtService.isTokenValid(token, sameUserDifferentCase)).isTrue();
    }

    @Test
    void isTokenValid_expiredToken_returnsFalse() throws Exception {
        // Create a JwtService with 1ms expiration
        JwtService shortLivedService = new JwtService();
        setField(shortLivedService, "secretKey", SECRET_KEY);
        setField(shortLivedService, "jwtExpiration", 1L);

        UserDetails user = createUser("test@example.com");
        String token = shortLivedService.generateToken(user);

        // Sleep to ensure token is expired
        Thread.sleep(100);
        // JJWT throws ExpiredJwtException when parsing expired tokens
        assertThatThrownBy(() -> shortLivedService.isTokenValid(token, user))
                .isInstanceOf(io.jsonwebtoken.ExpiredJwtException.class);
    }

    // === Invalid token handling ===

    @Test
    void extractUsername_invalidToken_throwsException() {
        assertThatThrownBy(() -> jwtService.extractUsername("invalid.token.here"))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractUsername_tamperedToken_throwsException() {
        UserDetails user = createUser("test@example.com");
        String token = jwtService.generateToken(user);
        // Tamper with the token
        String tampered = token.substring(0, token.length() - 5) + "XXXXX";
        assertThatThrownBy(() -> jwtService.extractUsername(tampered))
                .isInstanceOf(Exception.class);
    }

    @Test
    void extractUsername_tokenSignedWithDifferentKey_throwsException() {
        // Create a token with a different key (must be at least 256 bits for HS256)
        String differentKey = Base64.getEncoder().encodeToString(
                "AnotherDifferentSecretKey12345!!".getBytes());
        byte[] keyBytes = Decoders.BASE64.decode(differentKey);
        SecretKey key = Keys.hmacShaKeyFor(keyBytes);

        String token = Jwts.builder()
                .subject("test@example.com")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 3600000))
                .signWith(key)
                .compact();

        assertThatThrownBy(() -> jwtService.extractUsername(token))
                .isInstanceOf(Exception.class);
    }
}
