package com.omegafrog.My.piano.app.web.exception.payment;

import org.springframework.http.ResponseEntity;

/**
 * 토스 API에서 반환된 오류에 대한 예외
 */
public class TossAPIException extends RuntimeException {
    private ResponseEntity<String> response;
    public TossAPIException(String message){
        super(message);
    }
    public TossAPIException(String message, ResponseEntity<String> response){
        super(message);
        this.response = response;

    }
}
