package com.omegafrog.My.piano.app.web.exception.payment;

/**
 * 토스 API에서 반환된 오류에 대한 예외
 */
public class TossAPIException extends RuntimeException {
    public TossAPIException(String message){
        super(message);
    }
}
