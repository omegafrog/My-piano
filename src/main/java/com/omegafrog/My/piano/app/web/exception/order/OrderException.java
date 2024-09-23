package com.omegafrog.My.piano.app.web.exception.order;

public abstract class OrderException extends RuntimeException{
    public OrderException(String message) {
        super(message);
    }
    public OrderException(String message, Throwable cause) {
        super(message, cause);
    }
}
