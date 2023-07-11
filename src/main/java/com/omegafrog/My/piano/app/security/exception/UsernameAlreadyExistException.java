package com.omegafrog.My.piano.app.security.exception;

public class UsernameAlreadyExistException extends Throwable {
    public UsernameAlreadyExistException(String message, Throwable cause) {
        super(message, cause);
    }

    public UsernameAlreadyExistException(String message) {
        super(message);
    }
}
