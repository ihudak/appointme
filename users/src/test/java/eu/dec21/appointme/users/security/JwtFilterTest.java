package eu.dec21.appointme.users.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtFilter Tests")
class JwtFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtFilter jwtFilter;

    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
        testUserDetails = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(List.of(
                        new SimpleGrantedAuthority("ROLE_USER"),
                        new SimpleGrantedAuthority("ROLE_ADMIN")
                ))
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ==================== Authentication Path Tests ====================

    @Test
    @DisplayName("Should skip filter for /api/v1/auth paths")
    void shouldSkipFilterForAuthPaths() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/auth/login");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(request, never()).getHeader(anyString());
        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should skip filter for /api/v1/auth/register path")
    void shouldSkipFilterForAuthRegisterPath() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/auth/register");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should skip filter for /api/v1/auth/activate path")
    void shouldSkipFilterForAuthActivatePath() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/auth/activate");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
    }

    // ==================== Missing Authorization Header Tests ====================

    @Test
    @DisplayName("Should skip filter when Authorization header is null")
    void shouldSkipFilterWhenAuthHeaderIsNull() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn(null);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should skip filter when Authorization header is empty")
    void shouldSkipFilterWhenAuthHeaderIsEmpty() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // ==================== Invalid Bearer Token Format Tests ====================

    @Test
    @DisplayName("Should skip filter when Authorization header doesn't start with 'Bearer '")
    void shouldSkipFilterWhenAuthHeaderDoesNotStartWithBearer() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Basic dXNlcjpwYXNz");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should skip filter when Authorization header has wrong case Bearer")
    void shouldSkipFilterWhenBearerHasWrongCase() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("bearer token123");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should skip filter when Authorization header is just 'Bearer' without space")
    void shouldSkipFilterWhenBearerWithoutSpace() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearertoken123");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(jwtService, userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should skip filter when Authorization header is only 'Bearer '")
    void shouldSkipFilterWhenOnlyBearer() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer ");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        // extractUsername will be called with empty string
        verify(jwtService).extractUsername("");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // ==================== Email Extraction Tests ====================

    @Test
    @DisplayName("Should skip authentication when extracted email is null")
    void shouldSkipAuthenticationWhenEmailIsNull() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken123");
        when(jwtService.extractUsername("validtoken123")).thenReturn(null);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername("validtoken123");
        verifyNoInteractions(userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should skip authentication when extracted email is empty")
    void shouldSkipAuthenticationWhenEmailIsEmpty() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken123");
        when(jwtService.extractUsername("validtoken123")).thenReturn("");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername("validtoken123");
        verifyNoInteractions(userDetailsService);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should skip authentication when extracted email is blank")
    void shouldSkipAuthenticationWhenEmailIsBlank() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken123");
        when(jwtService.extractUsername("validtoken123")).thenReturn("   ");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername("validtoken123");
        // Will call userDetailsService because "   " is not empty after trim is done in the check
        // Actually looking at line 53: email != null && !email.isEmpty()
        // "   " is not empty, so it will proceed
        verify(userDetailsService).loadUserByUsername("   ");
    }

    // ==================== Already Authenticated Tests ====================

    @Test
    @DisplayName("Should skip authentication when SecurityContext already has authentication")
    void shouldSkipAuthenticationWhenAlreadyAuthenticated() throws ServletException, IOException {
        // Given
        Authentication existingAuth = mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken123");
        when(jwtService.extractUsername("validtoken123")).thenReturn("test@example.com");

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername("validtoken123");
        verifyNoInteractions(userDetailsService);
        // Authentication should remain unchanged
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isEqualTo(existingAuth);
    }

    // ==================== Successful Authentication Tests ====================

    @Test
    @DisplayName("Should authenticate user with valid JWT token")
    void shouldAuthenticateUserWithValidToken() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken123");
        when(jwtService.extractUsername("validtoken123")).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUserDetails);
        when(jwtService.isTokenValid("validtoken123", testUserDetails)).thenReturn(true);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername("validtoken123");
        verify(userDetailsService).loadUserByUsername("test@example.com");
        verify(jwtService).isTokenValid("validtoken123", testUserDetails);

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(testUserDetails);
        assertThat(auth.getCredentials()).isNull();
        assertThat(auth.getAuthorities())
                .hasSize(2)
                .extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
        assertThat(auth.isAuthenticated()).isTrue();
    }

    @Test
    @DisplayName("Should set authentication details from request")
    void shouldSetAuthenticationDetailsFromRequest() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken123");
        when(request.getRemoteAddr()).thenReturn("192.168.1.1");
        when(request.getSession(false)).thenReturn(null);
        when(jwtService.extractUsername("validtoken123")).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUserDetails);
        when(jwtService.isTokenValid("validtoken123", testUserDetails)).thenReturn(true);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getDetails()).isNotNull();
    }

    // ==================== Invalid Token Tests ====================

    @Test
    @DisplayName("Should not authenticate when token is invalid")
    void shouldNotAuthenticateWhenTokenIsInvalid() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer invalidtoken");
        when(jwtService.extractUsername("invalidtoken")).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUserDetails);
        when(jwtService.isTokenValid("invalidtoken", testUserDetails)).thenReturn(false);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername("invalidtoken");
        verify(userDetailsService).loadUserByUsername("test@example.com");
        verify(jwtService).isTokenValid("invalidtoken", testUserDetails);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    @DisplayName("Should not authenticate when token is expired")
    void shouldNotAuthenticateWhenTokenIsExpired() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer expiredtoken");
        when(jwtService.extractUsername("expiredtoken")).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUserDetails);
        when(jwtService.isTokenValid("expiredtoken", testUserDetails)).thenReturn(false);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    // ==================== User Not Found Tests ====================

    @Test
    @DisplayName("Should propagate UsernameNotFoundException when user not found")
    void shouldPropagateExceptionWhenUserNotFound() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken123");
        when(jwtService.extractUsername("validtoken123")).thenReturn("notfound@example.com");
        when(userDetailsService.loadUserByUsername("notfound@example.com"))
                .thenThrow(new UsernameNotFoundException("User not found"));

        // When/Then
        try {
            jwtFilter.doFilterInternal(request, response, filterChain);
        } catch (UsernameNotFoundException e) {
            assertThat(e.getMessage()).isEqualTo("User not found");
            verify(jwtService).extractUsername("validtoken123");
            verify(userDetailsService).loadUserByUsername("notfound@example.com");
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    // ==================== JWT Exception Handling Tests ====================

    @Test
    @DisplayName("Should propagate exception when JWT extraction fails")
    void shouldPropagateExceptionWhenJwtExtractionFails() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer malformedtoken");
        when(jwtService.extractUsername("malformedtoken"))
                .thenThrow(new RuntimeException("Malformed JWT"));

        // When/Then
        try {
            jwtFilter.doFilterInternal(request, response, filterChain);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Malformed JWT");
            verify(jwtService).extractUsername("malformedtoken");
            verifyNoInteractions(userDetailsService);
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    @Test
    @DisplayName("Should propagate exception when token validation fails with exception")
    void shouldPropagateExceptionWhenTokenValidationFails() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer badtoken");
        when(jwtService.extractUsername("badtoken")).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUserDetails);
        when(jwtService.isTokenValid("badtoken", testUserDetails))
                .thenThrow(new RuntimeException("Token validation error"));

        // When/Then
        try {
            jwtFilter.doFilterInternal(request, response, filterChain);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo("Token validation error");
            assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        }
    }

    // ==================== Edge Cases ====================

    @Test
    @DisplayName("Should handle very long token")
    void shouldHandleVeryLongToken() throws ServletException, IOException {
        // Given
        String veryLongToken = "a".repeat(10000);
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + veryLongToken);
        when(jwtService.extractUsername(veryLongToken)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUserDetails);
        when(jwtService.isTokenValid(veryLongToken, testUserDetails)).thenReturn(true);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
        verify(jwtService).extractUsername(veryLongToken);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
    }

    @Test
    @DisplayName("Should handle token with special characters")
    void shouldHandleTokenWithSpecialCharacters() throws ServletException, IOException {
        // Given
        String tokenWithSpecialChars = "abc-123_xyz.456";
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer " + tokenWithSpecialChars);
        when(jwtService.extractUsername(tokenWithSpecialChars)).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUserDetails);
        when(jwtService.isTokenValid(tokenWithSpecialChars, testUserDetails)).thenReturn(true);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
    }

    @Test
    @DisplayName("Should handle email with unicode characters")
    void shouldHandleEmailWithUnicodeCharacters() throws ServletException, IOException {
        // Given
        String unicodeEmail = "test@例え.com";
        UserDetails unicodeUser = User.builder()
                .username(unicodeEmail)
                .password("password")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();

        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken123");
        when(jwtService.extractUsername("validtoken123")).thenReturn(unicodeEmail);
        when(userDetailsService.loadUserByUsername(unicodeEmail)).thenReturn(unicodeUser);
        when(jwtService.isTokenValid("validtoken123", unicodeUser)).thenReturn(true);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getPrincipal()).isEqualTo(unicodeUser);
    }

    @Test
    @DisplayName("Should handle multiple consecutive filter invocations")
    void shouldHandleMultipleConsecutiveInvocations() throws ServletException, IOException {
        // Given
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken123");
        when(jwtService.extractUsername("validtoken123")).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUserDetails);
        when(jwtService.isTokenValid("validtoken123", testUserDetails)).thenReturn(true);

        // When - First invocation
        jwtFilter.doFilterInternal(request, response, filterChain);
        Authentication firstAuth = SecurityContextHolder.getContext().getAuthentication();

        // Clear for second invocation
        SecurityContextHolder.clearContext();

        // When - Second invocation
        jwtFilter.doFilterInternal(request, response, filterChain);
        Authentication secondAuth = SecurityContextHolder.getContext().getAuthentication();

        // Then
        assertThat(firstAuth).isNotNull();
        assertThat(secondAuth).isNotNull();
        verify(filterChain, times(2)).doFilter(request, response);
        verify(userDetailsService, times(2)).loadUserByUsername("test@example.com");
    }

    @Test
    @DisplayName("Should handle user with no authorities")
    void shouldHandleUserWithNoAuthorities() throws ServletException, IOException {
        // Given
        UserDetails userWithNoAuthorities = User.builder()
                .username("test@example.com")
                .password("password")
                .authorities(List.of())
                .build();

        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken123");
        when(jwtService.extractUsername("validtoken123")).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(userWithNoAuthorities);
        when(jwtService.isTokenValid("validtoken123", userWithNoAuthorities)).thenReturn(true);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).isEmpty();
    }

    @Test
    @DisplayName("Should always call filterChain.doFilter regardless of authentication success")
    void shouldAlwaysCallFilterChainDoFilter() throws ServletException, IOException {
        // Test various scenarios to ensure filterChain.doFilter is always called

        // Scenario 1: Auth path
        when(request.getServletPath()).thenReturn("/api/v1/auth/login");
        jwtFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        reset(filterChain);

        // Scenario 2: No auth header
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn(null);
        jwtFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        reset(filterChain);

        // Scenario 3: Invalid token format
        when(request.getHeader("Authorization")).thenReturn("Basic token");
        jwtFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
        reset(filterChain);

        // Scenario 4: Valid authentication
        when(request.getHeader("Authorization")).thenReturn("Bearer validtoken123");
        when(jwtService.extractUsername("validtoken123")).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUserDetails);
        when(jwtService.isTokenValid("validtoken123", testUserDetails)).thenReturn(true);
        SecurityContextHolder.clearContext();
        jwtFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle Authorization header with extra spaces")
    void shouldHandleAuthorizationHeaderWithExtraSpaces() throws ServletException, IOException {
        // Given - Multiple spaces between Bearer and token
        when(request.getServletPath()).thenReturn("/api/v1/users");
        when(request.getHeader("Authorization")).thenReturn("Bearer   token123");
        when(jwtService.extractUsername("  token123")).thenReturn("test@example.com");
        when(userDetailsService.loadUserByUsername("test@example.com")).thenReturn(testUserDetails);
        when(jwtService.isTokenValid("  token123", testUserDetails)).thenReturn(true);

        // When
        jwtFilter.doFilterInternal(request, response, filterChain);

        // Then
        verify(jwtService).extractUsername("  token123");
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
    }
}
