package eu.dec21.appointme.exceptions;

public class OperationNotPermittedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public OperationNotPermittedException(String message) {
        super(message);
    }

    public OperationNotPermittedException() {
        super("Operation not permitted");
    }
}
