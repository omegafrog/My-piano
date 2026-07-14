package com.omegafrog.My.piano.app.web.domain.coupon.condition;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("OWNER")
@NoArgsConstructor
public class CouponOwnerCondition extends CouponValidationCondition {
    public CouponOwnerCondition(int evaluationOrder) { super(evaluationOrder); }
    @Override public CouponValidationResult evaluate(CouponValidationContext context) {
        return context.couponOwnerUserId().equals(context.applyingUserId()) ? CouponValidationResult.success()
                : CouponValidationResult.failure("OWNER", "COUPON_OWNER_MISMATCH");
    }
}
