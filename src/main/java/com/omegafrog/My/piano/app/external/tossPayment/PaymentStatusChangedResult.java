package com.omegafrog.My.piano.app.external.tossPayment;

import lombok.Getter;

public class PaymentStatusChangedResult extends TossWebHookResult{

    @Getter
    private final Payment data;

    public PaymentStatusChangedResult(String eventType, String createdAt, Payment data ) {
        super(eventType, createdAt);
        this.data = data;
    }
}
