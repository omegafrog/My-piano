package com.omegafrog.My.piano.app.external.tossPayment;

import lombok.Getter;

@Getter
public abstract class TossWebHookResult {
    private final String eventType;
    private final String createdAt;
    public TossWebHookResult(String eventType, String createdAt) {
        this.eventType = eventType;
        this.createdAt = createdAt;

    }
}
