package eu.dec21.appointme.common.config;

import eu.dec21.appointme.common.util.UserDetailsTestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationAuditAwareTest {

    private final ApplicationAuditAware auditAware = new ApplicationAuditAware();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentAuditor_noAuth_returnsEmpty() {
        Optional<Long> auditor = auditAware.getCurrentAuditor();
        assertThat(auditor).isEmpty();
    }

    @Test
    void getCurrentAuditor_authenticated_returnsUserId() {
        UserDetailsTestHelper principal = new UserDetailsTestHelper(42L);
        var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Optional<Long> auditor = auditAware.getCurrentAuditor();
        assertThat(auditor).isPresent().contains(42L);
    }

    @Test
    void implementsAuditorAware() {
        assertThat(auditAware).isInstanceOf(org.springframework.data.domain.AuditorAware.class);
    }
}
