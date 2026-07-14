package com.omegafrog.My.piano.app.web.domain.coupon;

public interface ProcessedPaymentEventRepository {
    boolean existsByEventId(String eventId);
    ProcessedPaymentEvent save(ProcessedPaymentEvent processedPaymentEvent);
}
