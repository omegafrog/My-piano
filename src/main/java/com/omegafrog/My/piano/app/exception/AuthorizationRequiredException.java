package com.omegafrog.My.piano.app.exception;

import org.springframework.security.core.AuthenticationException;

public class AuthorizationRequiredException extends AuthenticationException {
    public AuthorizationRequiredException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public AuthorizationRequiredException(String msg) {
        super(msg);
    }
}
