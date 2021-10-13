package de.xab.porter.api.exception;

/**
 * a runtime exception to porter project,
 * suppressed any exception thrown from deeper layer.
 */
public final class PorterException extends RuntimeException {
    public PorterException() {
    }

    public PorterException(String message) {
        super(message);
    }

    public PorterException(String message, Throwable cause) {
        super(message, cause);
    }

    public PorterException(Throwable cause) {
        super(cause);
    }
}
