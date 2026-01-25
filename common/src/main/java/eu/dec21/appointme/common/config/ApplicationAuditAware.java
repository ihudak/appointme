package eu.dec21.appointme.common.config;

import eu.dec21.appointme.common.util.SecurityUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@NullMarked
public class ApplicationAuditAware implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        return SecurityUtils.getCurrentUserId();
    }
}
