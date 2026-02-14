package eu.dec21.appointme.exceptions;

public class DuplicateResourceException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public DuplicateResourceException(String message) {
        super(message);
    }
}
