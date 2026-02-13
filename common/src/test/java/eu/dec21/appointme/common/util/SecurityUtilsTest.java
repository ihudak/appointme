package eu.dec21.appointme.common.util;

import eu.dec21.appointme.exceptions.UserAuthenticationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityUtilsTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    // === getUserIdFromAuthentication ===

    @Test
    void getUserIdFromAuthentication_nullAuthentication_returnsEmpty() {
        Optional<Long> result = SecurityUtils.getUserIdFromAuthentication(null);
        assertThat(result).isEmpty();
    }

    @Test
    void getUserIdFromAuthentication_notAuthenticated_returnsEmpty() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        Optional<Long> result = SecurityUtils.getUserIdFromAuthentication(auth);
        assertThat(result).isEmpty();
    }

    @Test
    void getUserIdFromAuthentication_anonymousToken_returnsEmpty() {
        AnonymousAuthenticationToken auth = new AnonymousAuthenticationToken(
                "key", "anonymous", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));

        Optional<Long> result = SecurityUtils.getUserIdFromAuthentication(auth);
        assertThat(result).isEmpty();
    }

    @Test
    void getUserIdFromAuthentication_nullPrincipal_returnsEmpty() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(null);

        Optional<Long> result = SecurityUtils.getUserIdFromAuthentication(auth);
        assertThat(result).isEmpty();
    }

    @Test
    void getUserIdFromAuthentication_principalWithGetId_returnsId() {
        UserDetailsTestHelper principal = new UserDetailsTestHelper(42L);
        // 3-arg constructor sets authenticated=true
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());

        Optional<Long> result = SecurityUtils.getUserIdFromAuthentication(auth);
        assertThat(result).isPresent().contains(42L);
    }

    @Test
    void getUserIdFromAuthentication_principalWithoutGetId_returnsEmpty() {
        // String principal has no getId() method
        Authentication auth = new UsernamePasswordAuthenticationToken("user@example.com", null);

        Optional<Long> result = SecurityUtils.getUserIdFromAuthentication(auth);
        assertThat(result).isEmpty();
    }

    // === getUserIdFromAuthenticationOrThrow ===

    @Test
    void getUserIdFromAuthenticationOrThrow_authenticated_returnsId() {
        UserDetailsTestHelper principal = new UserDetailsTestHelper(99L);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());

        Long result = SecurityUtils.getUserIdFromAuthenticationOrThrow(auth);
        assertThat(result).isEqualTo(99L);
    }

    @Test
    void getUserIdFromAuthenticationOrThrow_notAuthenticated_throws() {
        assertThatThrownBy(() -> SecurityUtils.getUserIdFromAuthenticationOrThrow(null))
                .isInstanceOf(UserAuthenticationException.class);
    }

    // === getCurrentUserId ===

    @Test
    void getCurrentUserId_noSecurityContext_returnsEmpty() {
        Optional<Long> result = SecurityUtils.getCurrentUserId();
        assertThat(result).isEmpty();
    }

    @Test
    void getCurrentUserId_authenticated_returnsId() {
        UserDetailsTestHelper principal = new UserDetailsTestHelper(7L);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<Long> result = SecurityUtils.getCurrentUserId();
        assertThat(result).isPresent().contains(7L);
    }

    // === getCurrentUserIdOrThrow ===

    @Test
    void getCurrentUserIdOrThrow_noAuth_throws() {
        assertThatThrownBy(SecurityUtils::getCurrentUserIdOrThrow)
                .isInstanceOf(UserAuthenticationException.class);
    }

    @Test
    void getCurrentUserIdOrThrow_authenticated_returnsId() {
        UserDetailsTestHelper principal = new UserDetailsTestHelper(55L);
        Authentication auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Long result = SecurityUtils.getCurrentUserIdOrThrow();
        assertThat(result).isEqualTo(55L);
    }

    // === hasRole ===

    @Test
    void hasRole_nullAuth_returnsFalse() {
        assertThat(SecurityUtils.hasRole(null, "ADMIN")).isFalse();
    }

    @Test
    void hasRole_notAuthenticated_returnsFalse() {
        Authentication auth = mock(Authentication.class);
        when(auth.isAuthenticated()).thenReturn(false);

        assertThat(SecurityUtils.hasRole(auth, "ADMIN")).isFalse();
    }

    @Test
    void hasRole_withMatchingRole_returnsTrue() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        assertThat(SecurityUtils.hasRole(auth, "ADMIN")).isTrue();
    }

    @Test
    void hasRole_withRolePrefixAlreadyIncluded_returnsTrue() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        assertThat(SecurityUtils.hasRole(auth, "ROLE_ADMIN")).isTrue();
    }

    @Test
    void hasRole_withNonMatchingRole_returnsFalse() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        assertThat(SecurityUtils.hasRole(auth, "ADMIN")).isFalse();
    }

    @Test
    void hasRole_anonymous_returnsFalse() {
        AnonymousAuthenticationToken auth = new AnonymousAuthenticationToken(
                "key", "anon", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));

        assertThat(SecurityUtils.hasRole(auth, "ADMIN")).isFalse();
    }

    // === isAdmin ===

    @Test
    void isAdmin_withAdminRole_returnsTrue() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        assertThat(SecurityUtils.isAdmin(auth)).isTrue();
    }

    @Test
    void isAdmin_withoutAdminRole_returnsFalse() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("ROLE_USER")));

        assertThat(SecurityUtils.isAdmin(auth)).isFalse();
    }

    @Test
    void isAdmin_nullAuth_returnsFalse() {
        assertThat(SecurityUtils.isAdmin(null)).isFalse();
    }

    // === isCurrentUserAdmin ===

    @Test
    void isCurrentUserAdmin_withAdminInContext_returnsTrue() {
        Authentication auth = new UsernamePasswordAuthenticationToken(
                "user", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThat(SecurityUtils.isCurrentUserAdmin()).isTrue();
    }

    @Test
    void isCurrentUserAdmin_noContext_returnsFalse() {
        assertThat(SecurityUtils.isCurrentUserAdmin()).isFalse();
    }

    // === Private constructor ===

    @Test
    void constructor_throwsUnsupportedOperationException() {
        assertThatThrownBy(() -> {
            var constructor = SecurityUtils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            constructor.newInstance();
        }).isInstanceOf(InvocationTargetException.class)
          .hasCauseInstanceOf(UnsupportedOperationException.class);
    }
}
