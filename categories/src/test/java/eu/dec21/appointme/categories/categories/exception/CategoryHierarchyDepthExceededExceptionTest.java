package eu.dec21.appointme.categories.categories.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CategoryHierarchyDepthExceededException Tests")
class CategoryHierarchyDepthExceededExceptionTest {

    @Test
    @DisplayName("Should create exception with max depth only")
    void shouldCreateExceptionWithMaxDepthOnly() {
        int maxDepth = 10;
        
        CategoryHierarchyDepthExceededException exception = 
                new CategoryHierarchyDepthExceededException(maxDepth);
        
        assertThat(exception).isInstanceOf(RuntimeException.class);
        assertThat(exception.getMessage())
                .contains("exceeded maximum allowed depth of 10")
                .contains("circular reference");
    }

    @Test
    @DisplayName("Should create exception with category ID and depths")
    void shouldCreateExceptionWithCategoryIdAndDepths() {
        Long categoryId = 42L;
        int maxDepth = 5;
        int currentDepth = 7;
        
        CategoryHierarchyDepthExceededException exception = 
                new CategoryHierarchyDepthExceededException(categoryId, maxDepth, currentDepth);
        
        assertThat(exception.getMessage())
                .contains("Category 42")
                .contains("depth (7)")
                .contains("maximum allowed depth of 5");
    }

    @Test
    @DisplayName("Should format message correctly with all parameters")
    void shouldFormatMessageCorrectlyWithAllParameters() {
        CategoryHierarchyDepthExceededException exception = 
                new CategoryHierarchyDepthExceededException(100L, 10, 15);
        
        assertThat(exception.getMessage())
                .isEqualTo("Category 100: hierarchy depth (15) exceeded maximum allowed depth of 10 levels");
    }

    @Test
    @DisplayName("Should be throwable as RuntimeException")
    void shouldBeThrowableAsRuntimeException() {
        CategoryHierarchyDepthExceededException exception = 
                new CategoryHierarchyDepthExceededException(5);
        
        assertThat(exception).isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("Should handle max depth of 1")
    void shouldHandleMaxDepthOfOne() {
        CategoryHierarchyDepthExceededException exception = 
                new CategoryHierarchyDepthExceededException(1);
        
        assertThat(exception.getMessage())
                .contains("exceeded maximum allowed depth of 1 level");
    }

    @Test
    @DisplayName("Should handle very large depths")
    void shouldHandleVeryLargeDepths() {
        CategoryHierarchyDepthExceededException exception = 
                new CategoryHierarchyDepthExceededException(Long.MAX_VALUE, 100, 1000);
        
        assertThat(exception.getMessage())
                .contains(String.valueOf(Long.MAX_VALUE))
                .contains("1000")
                .contains("100");
    }

    @Test
    @DisplayName("Should mention circular reference possibility in general constructor")
    void shouldMentionCircularReferencePossibilityInGeneralConstructor() {
        CategoryHierarchyDepthExceededException exception = 
                new CategoryHierarchyDepthExceededException(5);
        
        assertThat(exception.getMessage())
                .contains("circular reference")
                .contains("excessively deep hierarchy");
    }
}
