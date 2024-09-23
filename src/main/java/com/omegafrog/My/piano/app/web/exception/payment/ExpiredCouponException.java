package com.omegafrog.My.piano.app.web.exception.payment;

public class ExpiredCouponException extends RuntimeException {
    public ExpiredCouponException(String s) {
        super(s);
    }
}
