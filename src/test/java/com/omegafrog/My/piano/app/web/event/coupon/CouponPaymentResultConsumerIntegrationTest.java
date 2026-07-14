package com.omegafrog.My.piano.app.web.event.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.omegafrog.My.piano.app.web.domain.coupon.Coupon;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponApplication;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponApplicationRepository;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponApplicationStatus;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponRepository;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponUsageStatus;
import com.omegafrog.My.piano.app.web.domain.coupon.DiscountTerms;
import com.omegafrog.My.piano.app.web.domain.coupon.DiscountType;
import com.omegafrog.My.piano.app.web.domain.coupon.ProcessedPaymentEventRepository;
import com.omegafrog.My.piano.app.web.domain.coupon.condition.CouponOwnerCondition;
import com.omegafrog.My.piano.app.web.domain.coupon.condition.CouponUsageStatusCondition;
import com.omegafrog.My.piano.app.web.domain.coupon.condition.CouponValidityPeriodCondition;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.event.payment.PaymentResultEvent;
import com.omegafrog.My.piano.app.web.service.CouponPaymentResultApplicationService;

class CouponPaymentResultConsumerIntegrationTest {
    @Test
    void successEventMarksTemporaryCouponAndApplicationUsed() {
        Fixture fixture = new Fixture();
        when(fixture.applicationRepository.findByOrderId(10L)).thenReturn(Optional.of(fixture.application));
        when(fixture.couponRepository.findById("coupon-1")).thenReturn(Optional.of(fixture.coupon));

        fixture.consumer.handlePaymentSucceeded(fixture.event("event-success", "10"));

        assertThat(fixture.coupon.getUsageStatus()).isEqualTo(CouponUsageStatus.USED);
        assertThat(fixture.application.getStatus()).isEqualTo(CouponApplicationStatus.USED);
        verify(fixture.processedEventRepository).save(any());
    }

    @Test
    void failedEventRestoresTemporaryCouponAndApplication() {
        Fixture fixture = new Fixture();
        when(fixture.applicationRepository.findByOrderId(10L)).thenReturn(Optional.of(fixture.application));
        when(fixture.couponRepository.findById("coupon-1")).thenReturn(Optional.of(fixture.coupon));

        fixture.consumer.handlePaymentFailed(fixture.event("event-failed", "10"));

        assertThat(fixture.coupon.getUsageStatus()).isEqualTo(CouponUsageStatus.AVAILABLE);
        assertThat(fixture.application.getStatus()).isEqualTo(CouponApplicationStatus.RESTORED);
    }

    @Test
    void duplicateEventIsIgnoredBeforeStateTransition() {
        Fixture fixture = new Fixture();
        when(fixture.processedEventRepository.existsByEventId("event-duplicate")).thenReturn(true);

        fixture.consumer.handlePaymentSucceeded(fixture.event("event-duplicate", "10"));

        verify(fixture.applicationRepository, never()).findByOrderId(any());
        verify(fixture.couponRepository, never()).save(any());
    }

    @Test
    void eventWithoutCouponApplicationIsSafelyIgnoredAndRecorded() {
        Fixture fixture = new Fixture();
        when(fixture.applicationRepository.findByOrderId(99L)).thenReturn(Optional.empty());

        fixture.consumer.handlePaymentSucceeded(fixture.event("event-no-application", "99"));

        verify(fixture.couponRepository, never()).findById(any());
        verify(fixture.processedEventRepository).save(any());
    }

    private static class Fixture {
        private final CouponRepository couponRepository = mock(CouponRepository.class);
        private final CouponApplicationRepository applicationRepository = mock(CouponApplicationRepository.class);
        private final ProcessedPaymentEventRepository processedEventRepository = mock(ProcessedPaymentEventRepository.class);
        private final CouponPaymentResultConsumer consumer = new CouponPaymentResultConsumer(
                new CouponPaymentResultApplicationService(couponRepository, applicationRepository, processedEventRepository)
        );
        private final User owner = owner();
        private final Coupon coupon = Coupon.issue(owner, "coupon", new DiscountTerms(DiscountType.FIXED_AMOUNT,
                BigDecimal.valueOf(1000), null), List.of(new CouponOwnerCondition(1),
                new CouponValidityPeriodCondition(2, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1)),
                new CouponUsageStatusCondition(3)));
        private final CouponApplication application;

        private Fixture() {
            application = CouponApplication.temporarilyApply(10L, "coupon-1", BigDecimal.valueOf(1000), LocalDateTime.now());
            coupon.temporarilyApply(owner.getId(), BigDecimal.valueOf(10000), LocalDateTime.now());
        }

        private PaymentResultEvent event(String eventId, String orderId) {
            return new PaymentResultEvent(eventId, orderId, LocalDateTime.now());
        }

        private static User owner() {
            User owner = new User();
            ReflectionTestUtils.setField(owner, "id", 1L);
            return owner;
        }
    }
}
