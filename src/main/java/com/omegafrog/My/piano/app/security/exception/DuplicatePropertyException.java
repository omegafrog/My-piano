package com.omegafrog.My.piano.app.security.exception;

public class DuplicatePropertyException extends RuntimeException {
    public DuplicatePropertyException(String message, Throwable cause) {
        super(message, cause);
    }

    public DuplicatePropertyException(String message) {
        super(message);
    }
}
