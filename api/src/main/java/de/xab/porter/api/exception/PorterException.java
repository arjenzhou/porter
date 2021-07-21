package de.xab.porter.api.exception;

/**
 * a runtime exception of porter project, suppressed any exception thrown from deeper layer
 */
public class PorterException extends RuntimeException {
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

    public PorterException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
