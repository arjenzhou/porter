package de.xab.porter.api.exception;

/**
 * indicates that the function was not supported
 */
public final class NotSupportedException extends IllegalArgumentException {
    public NotSupportedException(String s) {
        super(s);
    }
}
