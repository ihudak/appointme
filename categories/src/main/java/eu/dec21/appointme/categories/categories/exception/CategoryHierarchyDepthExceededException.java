package eu.dec21.appointme.categories.categories.exception;

/**
 * Exception thrown when the category hierarchy exceeds the maximum allowed depth.
 * This prevents stack overflow from extremely deep hierarchies and indicates
 * a potentially problematic data structure.
 */
public class CategoryHierarchyDepthExceededException extends RuntimeException {
    
    public CategoryHierarchyDepthExceededException(int maxDepth) {
        super(String.format("Category hierarchy depth exceeded maximum allowed depth of %d levels. " +
                "This may indicate a circular reference or excessively deep hierarchy.", maxDepth));
    }
    
    public CategoryHierarchyDepthExceededException(Long categoryId, int maxDepth, int currentDepth) {
        super(String.format("Category %d: hierarchy depth (%d) exceeded maximum allowed depth of %d levels", 
                categoryId, currentDepth, maxDepth));
    }
}
