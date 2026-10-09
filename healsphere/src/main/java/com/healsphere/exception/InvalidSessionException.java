package com.healsphere.exception;

/** Thrown when a submitted session breaks a business rule. Becomes HTTP 400. */
public class InvalidSessionException extends RuntimeException {
    public InvalidSessionException(String message) { super(message); }
}
