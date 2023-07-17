package com.omegafrog.My.piano.app.web.enums;

public enum TicketType {
    TYPE_LESSON("레슨 관련 문의"),
    TYPE_SHEET("악보 관련 문의"),
    TYPE_PAYMENT("결제 관련 문의");

    private String description;

    TicketType(String description) {
        this.description = description;
    }
}
