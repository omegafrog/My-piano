package com.omegafrog.My.piano.app.web.event.payment;

import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PaymentResultPublisher {
    private final KafkaTemplate<String, Object> couponKafkaTemplate;
    @Value("${coupon.kafka.payment-succeeded-topic:payment-succeeded}") private String succeededTopic;
    @Value("${coupon.kafka.payment-failed-topic:payment-failed}") private String failedTopic;
    public void publishSucceeded(String orderId) { publish(succeededTopic, orderId); }
    public void publishFailed(String orderId) { publish(failedTopic, orderId); }
    private void publish(String topic, String orderId) {
        PaymentResultEvent event = new PaymentResultEvent(UUID.randomUUID().toString(), orderId, LocalDateTime.now());
        couponKafkaTemplate.send(topic, orderId, event).join();
    }
}
