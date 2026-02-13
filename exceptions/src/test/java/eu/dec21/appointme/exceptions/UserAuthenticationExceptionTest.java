package eu.dec21.appointme.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class UserAuthenticationExceptionTest {

    @Test
    void constructor_withMessage() {
        UserAuthenticationException ex = new UserAuthenticationException("Auth failed");
        assertThat(ex.getReason()).isEqualTo("Auth failed");
        assertThat(ex.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void constructor_withMessageAndCause() {
        Throwable cause = new RuntimeException("JWT expired");
        UserAuthenticationException ex = new UserAuthenticationException("Auth failed", cause);
        assertThat(ex.getReason()).isEqualTo("Auth failed");
        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getStatusCode().value()).isEqualTo(401);
    }

    @Test
    void extendsResponseStatusException() {
        UserAuthenticationException ex = new UserAuthenticationException("test");
        assertThat(ex).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void statusCode_isUnauthorized() {
        UserAuthenticationException ex = new UserAuthenticationException("test");
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void constructor_withNullMessage() {
        UserAuthenticationException ex = new UserAuthenticationException(null);
        assertThat(ex.getReason()).isNull();
    }

    @Test
    void constructor_withNullMessageAndCause() {
        Throwable cause = new RuntimeException("cause");
        UserAuthenticationException ex = new UserAuthenticationException(null, cause);
        assertThat(ex.getReason()).isNull();
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}
