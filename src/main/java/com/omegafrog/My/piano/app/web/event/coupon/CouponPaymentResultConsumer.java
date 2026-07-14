package com.omegafrog.My.piano.app.web.event.coupon;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.omegafrog.My.piano.app.web.event.payment.PaymentResultEvent;
import com.omegafrog.My.piano.app.web.service.CouponPaymentResultApplicationService;

import lombok.RequiredArgsConstructor;

@Component
@ConditionalOnProperty(name = "coupon.kafka.enabled", havingValue = "true", matchIfMissing = true)
@RequiredArgsConstructor
public class CouponPaymentResultConsumer {
    private final CouponPaymentResultApplicationService couponPaymentResultApplicationService;

    @KafkaListener(
            topics = "${coupon.kafka.payment-succeeded-topic:payment-succeeded}",
            containerFactory = "couponKafkaListenerContainerFactory"
    )
    public void handlePaymentSucceeded(PaymentResultEvent event) {
        couponPaymentResultApplicationService.handlePaymentSucceeded(event);
    }

    @KafkaListener(
            topics = "${coupon.kafka.payment-failed-topic:payment-failed}",
            containerFactory = "couponKafkaListenerContainerFactory"
    )
    public void handlePaymentFailed(PaymentResultEvent event) {
        couponPaymentResultApplicationService.handlePaymentFailed(event);
    }
}
