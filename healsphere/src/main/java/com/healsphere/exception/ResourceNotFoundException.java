package com.healsphere.exception;

/** Thrown when a record does not exist. Becomes HTTP 404. */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) { super(message); }
}
