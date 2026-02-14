package eu.dec21.appointme.common.config;

import eu.dec21.appointme.common.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

@NullMarked
@Slf4j
public class ApplicationAuditAware implements AuditorAware<Long> {
    @Override
    public Optional<Long> getCurrentAuditor() {
        Optional<Long> auditor = SecurityUtils.getCurrentUserId();
        if (auditor.isPresent()) {
            log.debug("Auditor resolved: userId={}", auditor.get());
        } else {
            log.debug("No authenticated user found for auditing (system operation)");
        }
        return auditor;
    }
}
