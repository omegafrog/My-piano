package com.omegafrog.My.piano.app.web.event.payment;

import java.time.LocalDateTime;

public record PaymentResultEvent(String eventId, String orderId, LocalDateTime occurredAt) { }
