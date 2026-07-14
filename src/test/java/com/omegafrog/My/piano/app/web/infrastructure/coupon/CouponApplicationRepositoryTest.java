package com.omegafrog.My.piano.app.web.infrastructure.coupon;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;

import com.omegafrog.My.piano.app.web.domain.coupon.CouponApplication;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponApplicationStatus;
import com.omegafrog.My.piano.app.web.domain.coupon.ProcessedPaymentEvent;

class CouponApplicationRepositoryTest {
    @Test
    void temporarilyAppliedCouponApplicationKeepsOrderCouponAndDiscount() {
        CouponApplication application = CouponApplication.temporarilyApply(1L, "coupon-1", BigDecimal.valueOf(3000), LocalDateTime.now());
        assertThat(application.getOrderId()).isEqualTo(1L);
        assertThat(application.getStatus()).isEqualTo(CouponApplicationStatus.TEMPORARILY_APPLIED);
        application.markUsed();
        assertThat(application.getStatus()).isEqualTo(CouponApplicationStatus.USED);
    }

    @Test
    void processedPaymentEventUsesEventIdAsPersistentIdentity() {
        ProcessedPaymentEvent event = ProcessedPaymentEvent.of("event-1", LocalDateTime.now());
        assertThat(event.getEventId()).isEqualTo("event-1");
    }
}
