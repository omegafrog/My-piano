package com.omegafrog.My.piano.app.web.domain.coupon.condition;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("VALIDITY_PERIOD")
@NoArgsConstructor
public class CouponValidityPeriodCondition extends CouponValidationCondition {
    private LocalDateTime validFrom;
    private LocalDateTime validUntil;

    public CouponValidityPeriodCondition(int evaluationOrder, LocalDateTime validFrom, LocalDateTime validUntil) {
        super(evaluationOrder);
        this.validFrom = Objects.requireNonNull(validFrom, "validFrom must not be null");
        this.validUntil = Objects.requireNonNull(validUntil, "validUntil must not be null");
        if (!validUntil.isAfter(validFrom)) throw new IllegalArgumentException("validUntil must be after validFrom");
    }

    @Override public CouponValidationResult evaluate(CouponValidationContext context) {
        return !context.occurredAt().isBefore(validFrom) && !context.occurredAt().isAfter(validUntil)
                ? CouponValidationResult.success()
                : CouponValidationResult.failure("VALIDITY_PERIOD", "COUPON_EXPIRED_OR_NOT_STARTED");
    }
}
