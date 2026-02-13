package eu.dec21.appointme.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ActivationTokenExceptionTest {

    @Test
    void constructor_setsMessage() {
        ActivationTokenException ex = new ActivationTokenException("Token expired");
        assertThat(ex.getMessage()).isEqualTo("Token expired");
    }

    @Test
    void constructor_withNullMessage() {
        ActivationTokenException ex = new ActivationTokenException(null);
        assertThat(ex.getMessage()).isNull();
    }

    @Test
    void extendsRuntimeException() {
        ActivationTokenException ex = new ActivationTokenException("test");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void canBeThrownAndCaught() {
        try {
            throw new ActivationTokenException("Invalid token");
        } catch (ActivationTokenException e) {
            assertThat(e.getMessage()).isEqualTo("Invalid token");
        }
    }
}
