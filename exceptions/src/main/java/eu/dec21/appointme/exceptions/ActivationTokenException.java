package eu.dec21.appointme.exceptions;

public class ActivationTokenException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public ActivationTokenException(String message) {
        super(message);
    }
}
