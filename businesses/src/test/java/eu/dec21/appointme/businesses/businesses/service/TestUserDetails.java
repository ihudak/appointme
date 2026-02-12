package eu.dec21.appointme.businesses.businesses.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Public UserDetails implementation for tests.
 * Must be a public top-level class so that SecurityUtils can access getId() via reflection in Java 25.
 */
public class TestUserDetails implements UserDetails {
    private final Long id;

    public TestUserDetails(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return "password";
    }

    @Override
    public String getUsername() {
        return "user";
    }
}
