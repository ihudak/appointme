package eu.dec21.appointme.common.util;

import eu.dec21.appointme.exceptions.UserAuthenticationException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtils {

    private SecurityUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static Optional<Long> getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return getUserIdFromAuthentication(authentication);
    }

    public static Long getCurrentUserIdOrThrow() {
        return getCurrentUserId()
                .orElseThrow(() -> new UserAuthenticationException("User not authenticated"));
    }

    public static Optional<Long> getUserIdFromAuthentication(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        if (principal == null) {
            return Optional.empty();
        }

        if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            try {
                java.lang.reflect.Method getIdMethod = principal.getClass().getMethod("getId");
                Object id = getIdMethod.invoke(principal);
                if (id instanceof Long) {
                    return Optional.of((Long) id);
                }
            } catch (Exception e) {
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    public static Long getUserIdFromAuthenticationOrThrow(Authentication authentication) {
        return getUserIdFromAuthentication(authentication)
                .orElseThrow(() -> new UserAuthenticationException("User not authenticated"));
    }
}
