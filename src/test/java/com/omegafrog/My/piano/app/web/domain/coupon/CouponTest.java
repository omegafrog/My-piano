package com.omegafrog.My.piano.app.web.domain.coupon;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import com.omegafrog.My.piano.app.web.domain.coupon.condition.CouponOwnerCondition;
import com.omegafrog.My.piano.app.web.domain.coupon.condition.CouponUsageStatusCondition;
import com.omegafrog.My.piano.app.web.domain.coupon.condition.CouponValidityPeriodCondition;
import com.omegafrog.My.piano.app.web.domain.coupon.condition.MinimumOrderAmountCondition;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.exception.payment.CouponValidationException;

class CouponTest {
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 7, 14, 12, 0);

    @Test
    void validatesOrderedConditionsAndTemporarilyAppliesPercentageCouponWithCap() {
        Coupon coupon = coupon(DiscountType.PERCENTAGE, new BigDecimal("30"), new BigDecimal("2000"));

        BigDecimal discount = coupon.temporarilyApply(1L, new BigDecimal("10000"), NOW);

        assertThat(discount).isEqualByComparingTo("2000");
        assertThat(coupon.getUsageStatus()).isEqualTo(CouponUsageStatus.TEMPORARILY_APPLIED);
        coupon.markUsed();
        assertThat(coupon.getUsageStatus()).isEqualTo(CouponUsageStatus.USED);
    }

    @Test
    void stopsAtFirstFailedConditionInEvaluationOrder() {
        Coupon coupon = coupon(DiscountType.FIXED_AMOUNT, new BigDecimal("1000"), null);

        assertThatThrownBy(() -> coupon.temporarilyApply(2L, new BigDecimal("500"), NOW))
                .isInstanceOf(CouponValidationException.class)
                .hasMessage("COUPON_OWNER_MISMATCH");
    }

    @Test
    void rejectsReuseAndRestoresTemporaryApplication() {
        Coupon coupon = coupon(DiscountType.FIXED_AMOUNT, new BigDecimal("1000"), null);
        coupon.temporarilyApply(1L, new BigDecimal("10000"), NOW);

        assertThatThrownBy(() -> coupon.temporarilyApply(1L, new BigDecimal("10000"), NOW))
                .isInstanceOf(CouponValidationException.class)
                .hasMessage("COUPON_ALREADY_APPLIED_OR_USED");
        coupon.restore();
        assertThat(coupon.getUsageStatus()).isEqualTo(CouponUsageStatus.AVAILABLE);
    }

    @Test
    void rejectsCouponOutsideValidityPeriod() {
        Coupon coupon = coupon(DiscountType.FIXED_AMOUNT, new BigDecimal("1000"), null);

        assertThatThrownBy(() -> coupon.temporarilyApply(1L, new BigDecimal("10000"), NOW.plusDays(2)))
                .isInstanceOf(CouponValidationException.class)
                .hasMessage("COUPON_EXPIRED_OR_NOT_STARTED");
    }

    @Test
    void rejectsOrderBelowMinimumAmount() {
        Coupon coupon = coupon(DiscountType.FIXED_AMOUNT, new BigDecimal("1000"), null);

        assertThatThrownBy(() -> coupon.temporarilyApply(1L, new BigDecimal("999"), NOW))
                .isInstanceOf(CouponValidationException.class)
                .hasMessage("ORDER_AMOUNT_TOO_LOW");
    }

    private Coupon coupon(DiscountType type, BigDecimal value, BigDecimal maximumDiscountAmount) {
        User owner = new User();
        ReflectionTestUtils.setField(owner, "id", 1L);
        return Coupon.issue(owner, "할인 쿠폰", new DiscountTerms(type, value, maximumDiscountAmount), List.of(
                new CouponOwnerCondition(1),
                new CouponValidityPeriodCondition(2, NOW.minusDays(1), NOW.plusDays(1)),
                new CouponUsageStatusCondition(3),
                new MinimumOrderAmountCondition(4, new BigDecimal("1000"))));
    }
}
