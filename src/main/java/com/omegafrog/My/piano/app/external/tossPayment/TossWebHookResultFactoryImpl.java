package com.omegafrog.My.piano.app.external.tossPayment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class TossWebHookResultFactoryImpl implements TossWebHookResultFactory {

    private final ObjectMapper objectMapper;
    @Override
    public TossWebHookResult parse(String json) throws JsonProcessingException {
        String eventType = objectMapper.readTree(json).get("eventType").asText();
        String createdAt = objectMapper.readTree(json).get("createdAt").asText();
        Payment payment = objectMapper.convertValue(objectMapper.readTree(json).get("data"), Payment.class);
        return switch (eventType) {
            case "PAYMENT_STATUS_CHANGED" ->
                new PaymentStatusChangedResult(eventType, createdAt, payment);
            default -> throw new IllegalStateException("알 수 없는 이벤트 타입 값입니다.");
        };

    }
}
