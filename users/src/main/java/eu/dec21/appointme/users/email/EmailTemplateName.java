package eu.dec21.appointme.users.email;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailTemplateName {
    VERIFY_EMAIL("verify-email"),
    RESET_PASSWORD("reset-password")
    ;

    private final String name;
}
