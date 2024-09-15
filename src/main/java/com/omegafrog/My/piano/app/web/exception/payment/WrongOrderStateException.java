package com.omegafrog.My.piano.app.web.exception.payment;


public class WrongOrderStateException extends RuntimeException{
    public WrongOrderStateException(String message) {
        super(message);
    }

}