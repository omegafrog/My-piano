package com.omegafrog.My.piano.app.security.exception;

public class AlreadyRegisteredUserException extends RuntimeException{
    public AlreadyRegisteredUserException(String message) {
        super(message);
    }

    public AlreadyRegisteredUserException(String message, Throwable cause) {
        super(message, cause);
    }
}
