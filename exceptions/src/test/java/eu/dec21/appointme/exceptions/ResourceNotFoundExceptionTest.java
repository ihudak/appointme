package eu.dec21.appointme.exceptions;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;

class ResourceNotFoundExceptionTest {

    @Test
    void constructor_withMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Business not found");
        assertThat(ex.getReason()).isEqualTo("Business not found");
        assertThat(ex.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void constructor_withMessageAndCause() {
        Throwable cause = new RuntimeException("DB error");
        ResourceNotFoundException ex = new ResourceNotFoundException("Not found", cause);
        assertThat(ex.getReason()).isEqualTo("Not found");
        assertThat(ex.getCause()).isEqualTo(cause);
        assertThat(ex.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void extendsResponseStatusException() {
        ResourceNotFoundException ex = new ResourceNotFoundException("test");
        assertThat(ex).isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void statusCode_isNotFound() {
        ResourceNotFoundException ex = new ResourceNotFoundException("test");
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void constructor_withNullMessage() {
        ResourceNotFoundException ex = new ResourceNotFoundException(null);
        assertThat(ex.getReason()).isNull();
        assertThat(ex.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
