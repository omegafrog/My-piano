package com.omegafrog.My.piano.app.web.exception.payment;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public abstract class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }

    public PaymentException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaymentException(Throwable cause) {
        super(cause);
    }
}
