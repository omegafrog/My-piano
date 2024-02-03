package com.omegafrog.My.piano.app.web.enums;

public enum OrderStatus {
    READY("생성됨"),
    IN_PROGRESS("결제 진행중"),
    DONE("완료됨"),
    WAITING_FOR_DEPOSIT("입금 대기"),
    CANCELED("취소됨"),
    PARTIAL_CANCELED("부분 취소됨"),
    ABORTED("결제 승인 실패"),
    EXPIRED("유효 시간 초과");

    public final String description;

    OrderStatus(String description){
        this.description = description;
    }
}
