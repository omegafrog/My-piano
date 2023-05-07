package com.omegafrog.My.piano.app.enums;

public enum RefundPolicy {
    REFUND_IN_7DAYS("7일 이내에 이용하지 않은 레슨에 대해서 환불이 가능합니다."),
    REFUND_IN_15DAYS("15일 이내에 이용하지 않은 레슨에 대해서 환불이 가능합니다."),
    REFUND_IN_30DAYS("30일 이내에 이용하지 않은 레슨에 대해서 환불이 가능합니다.");

    final String description;

    RefundPolicy(String description) {
        this.description = description;
    }
}
