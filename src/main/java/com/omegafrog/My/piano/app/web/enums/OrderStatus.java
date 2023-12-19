package com.omegafrog.My.piano.app.web.enums;

public enum OrderStatus {
    CREATED("생성됨"),
    PROGRESSING("결제 진행중"),
    FINISHED("완료됨");

    public final String description;

    OrderStatus(String description){
        this.description = description;
    }
}
