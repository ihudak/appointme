package eu.dec21.appointme.categories.categories.exception;

/**
 * Exception thrown when a circular reference is detected in the category hierarchy.
 * This prevents infinite recursion and stack overflow errors.
 * 
 * Example: Category A has parent B, which has parent C, which has parent A (A→B→C→A)
 */
public class CircularCategoryReferenceException extends RuntimeException {
    
    public CircularCategoryReferenceException(String message) {
        super(message);
    }
    
    public CircularCategoryReferenceException(Long categoryId, Long parentId) {
        super(String.format("Circular reference detected: Category %d already visited in hierarchy when processing parent %d", 
                categoryId, parentId));
    }
}
