package com.omegafrog.My.piano.app.external.tossPayment;

/**
 * 토스 API 호출 실패 시 반환하는 에러 객체에 대한 레코드
 * @param code
 * @param message
 */

public record TossError(String code, String message) {

}