package eu.dec21.appointme.exceptions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("DuplicateResourceException")
class DuplicateResourceExceptionTest {

    @Test
    @DisplayName("Should create exception with message")
    void constructor_WithMessage_CreatesException() {
        // Given
        String message = "Resource already exists";

        // When
        DuplicateResourceException exception = new DuplicateResourceException(message);

        // Then
        assertThat(exception).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(message);
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should be a RuntimeException")
    void exception_IsRuntimeException() {
        // Given
        DuplicateResourceException exception = new DuplicateResourceException("test");

        // Then
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should support serialization")
    void exception_HasSerialVersionUID() throws NoSuchFieldException {
        // When
        var field = DuplicateResourceException.class.getDeclaredField("serialVersionUID");

        // Then
        assertThat(field).isNotNull();
        assertThat(field.getType()).isEqualTo(long.class);
    }
}
