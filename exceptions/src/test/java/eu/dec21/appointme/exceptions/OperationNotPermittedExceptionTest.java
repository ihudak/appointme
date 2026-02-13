package eu.dec21.appointme.exceptions;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class OperationNotPermittedExceptionTest {

    @Test
    void constructor_withMessage() {
        OperationNotPermittedException ex = new OperationNotPermittedException("Cannot delete");
        assertThat(ex.getMessage()).isEqualTo("Cannot delete");
    }

    @Test
    void noArgConstructor_hasDefaultMessage() {
        OperationNotPermittedException ex = new OperationNotPermittedException();
        assertThat(ex.getMessage()).isEqualTo("Operation not permitted");
    }

    @Test
    void extendsRuntimeException() {
        OperationNotPermittedException ex = new OperationNotPermittedException("test");
        assertThat(ex).isInstanceOf(RuntimeException.class);
    }

    @Test
    void constructor_withNullMessage() {
        OperationNotPermittedException ex = new OperationNotPermittedException(null);
        assertThat(ex.getMessage()).isNull();
    }
}
