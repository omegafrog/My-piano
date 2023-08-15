package com.omegafrog.My.piano.app.utils.exception.payment;

public class NotEnoughCashException extends PaymentException {
    public NotEnoughCashException() {
        super();
    }
    public NotEnoughCashException(String msg){
        super(msg);
    }
    public NotEnoughCashException(String msg, Throwable cause){
        super(msg, cause);
    }
    public NotEnoughCashException(Throwable cause){
        super(cause);
    }
}
