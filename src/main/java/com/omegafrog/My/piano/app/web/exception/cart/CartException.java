package com.omegafrog.My.piano.app.web.exception.cart;

public abstract class CartException extends RuntimeException{
    protected CartException(String message) {
        super(message);
    }
    protected CartException(String message, Throwable cause) {
        super(message, cause);
    }
}
