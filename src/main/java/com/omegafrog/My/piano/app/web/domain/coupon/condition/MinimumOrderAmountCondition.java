package com.omegafrog.My.piano.app.web.domain.coupon.condition;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("MINIMUM_ORDER_AMOUNT")
@NoArgsConstructor
public class MinimumOrderAmountCondition extends CouponValidationCondition {
    private BigDecimal minimumOrderAmount;

    public MinimumOrderAmountCondition(int evaluationOrder, BigDecimal minimumOrderAmount) {
        super(evaluationOrder);
        this.minimumOrderAmount = Objects.requireNonNull(minimumOrderAmount, "minimumOrderAmount must not be null");
        if (minimumOrderAmount.signum() < 0) throw new IllegalArgumentException("minimumOrderAmount must not be negative");
    }
    @Override public CouponValidationResult evaluate(CouponValidationContext context) {
        return context.orderAmount().compareTo(minimumOrderAmount) >= 0 ? CouponValidationResult.success()
                : CouponValidationResult.failure("MINIMUM_ORDER_AMOUNT", "ORDER_AMOUNT_TOO_LOW");
    }
}
