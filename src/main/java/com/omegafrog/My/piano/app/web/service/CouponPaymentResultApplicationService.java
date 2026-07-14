package com.omegafrog.My.piano.app.web.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.omegafrog.My.piano.app.web.domain.coupon.Coupon;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponApplication;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponApplicationRepository;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponRepository;
import com.omegafrog.My.piano.app.web.domain.coupon.ProcessedPaymentEvent;
import com.omegafrog.My.piano.app.web.domain.coupon.ProcessedPaymentEventRepository;
import com.omegafrog.My.piano.app.web.event.payment.PaymentResultEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponPaymentResultApplicationService {
    private final CouponRepository couponRepository;
    private final CouponApplicationRepository couponApplicationRepository;
    private final ProcessedPaymentEventRepository processedPaymentEventRepository;

    @Transactional
    public void handlePaymentSucceeded(PaymentResultEvent event) {
        handle(event, true);
    }

    @Transactional
    public void handlePaymentFailed(PaymentResultEvent event) {
        handle(event, false);
    }

    private void handle(PaymentResultEvent event, boolean succeeded) {
        if (processedPaymentEventRepository.existsByEventId(event.eventId())) {
            return;
        }

        Long orderId = parseOrderId(event.orderId());
        CouponApplication application = couponApplicationRepository.findByOrderId(orderId).orElse(null);
        if (application == null) {
            recordProcessed(event);
            return;
        }

        Coupon coupon = couponRepository.findById(application.getCouponId())
                .orElseThrow(() -> new IllegalStateException("coupon not found for payment result"));
        if (succeeded) {
            coupon.markUsed();
            application.markUsed();
        } else {
            coupon.restore();
            application.restore();
        }
        couponRepository.save(coupon);
        couponApplicationRepository.save(application);
        recordProcessed(event);
    }

    private Long parseOrderId(String orderId) {
        try {
            return Long.valueOf(orderId);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("payment result orderId must be a numeric order id", exception);
        }
    }

    private void recordProcessed(PaymentResultEvent event) {
        LocalDateTime processedAt = event.occurredAt() == null ? LocalDateTime.now() : event.occurredAt();
        processedPaymentEventRepository.save(ProcessedPaymentEvent.of(event.eventId(), processedAt));
    }
}
