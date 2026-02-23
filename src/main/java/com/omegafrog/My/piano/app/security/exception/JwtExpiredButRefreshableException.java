package com.omegafrog.My.piano.app.security.exception;

import org.springframework.security.authentication.CredentialsExpiredException;

public class JwtExpiredButRefreshableException extends CredentialsExpiredException {

    public JwtExpiredButRefreshableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
