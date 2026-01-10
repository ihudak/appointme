package eu.dec21.appointme.exceptions;

import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class UserAuthenticationException extends ResponseStatusException {
    private static final long serialVersionUID = 1L;

    public UserAuthenticationException(@Nullable String message, Throwable cause) {
        super(HttpStatus.UNAUTHORIZED, message, cause);
    }

    public UserAuthenticationException(@Nullable String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
