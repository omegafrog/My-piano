package com.omegafrog.My.piano.app.web.domain.coupon.condition;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import com.omegafrog.My.piano.app.web.domain.coupon.CouponUsageStatus;

public record CouponValidationContext(Long couponOwnerUserId, Long applyingUserId, BigDecimal orderAmount,
                                      LocalDateTime occurredAt, CouponUsageStatus couponUsageStatus) {
    public CouponValidationContext {
        Objects.requireNonNull(couponOwnerUserId, "couponOwnerUserId must not be null");
        Objects.requireNonNull(applyingUserId, "applyingUserId must not be null");
        Objects.requireNonNull(orderAmount, "orderAmount must not be null");
        Objects.requireNonNull(occurredAt, "occurredAt must not be null");
        Objects.requireNonNull(couponUsageStatus, "couponUsageStatus must not be null");
    }
}
