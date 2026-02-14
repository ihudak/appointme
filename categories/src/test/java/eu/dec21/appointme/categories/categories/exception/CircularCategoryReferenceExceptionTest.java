package eu.dec21.appointme.categories.categories.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CircularCategoryReferenceException Tests")
class CircularCategoryReferenceExceptionTest {

    @Test
    @DisplayName("Should create exception with custom message")
    void shouldCreateExceptionWithCustomMessage() {
        String message = "Circular reference in category tree";
        
        CircularCategoryReferenceException exception = 
                new CircularCategoryReferenceException(message);
        
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    @DisplayName("Should create exception with category IDs")
    void shouldCreateExceptionWithCategoryIds() {
        Long categoryId = 5L;
        Long parentId = 3L;
        
        CircularCategoryReferenceException exception = 
                new CircularCategoryReferenceException(categoryId, parentId);
        
        assertThat(exception.getMessage())
                .contains("Circular reference detected")
                .contains("5")
                .contains("3")
                .contains("already visited");
    }

    @Test
    @DisplayName("Should format message correctly with specific category and parent")
    void shouldFormatMessageCorrectlyWithSpecificCategoryAndParent() {
        CircularCategoryReferenceException exception = 
                new CircularCategoryReferenceException(10L, 7L);
        
        assertThat(exception.getMessage())
                .isEqualTo("Circular reference detected: Category 10 already visited in hierarchy when processing parent 7");
    }

    @Test
    @DisplayName("Should be throwable as RuntimeException")
    void shouldBeThrowableAsRuntimeException() {
        CircularCategoryReferenceException exception = 
                new CircularCategoryReferenceException(1L, 2L);
        
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should handle null in custom message")
    void shouldHandleNullInCustomMessage() {
        CircularCategoryReferenceException exception = 
                new CircularCategoryReferenceException((String) null);
        
        assertThat(exception.getMessage()).isNull();
    }

    @Test
    @DisplayName("Should handle very large category IDs")
    void shouldHandleVeryLargeCategoryIds() {
        Long veryLargeId = Long.MAX_VALUE;
        
        CircularCategoryReferenceException exception = 
                new CircularCategoryReferenceException(veryLargeId, veryLargeId - 1);
        
        assertThat(exception.getMessage())
                .contains(String.valueOf(veryLargeId))
                .contains(String.valueOf(veryLargeId - 1));
    }
}
