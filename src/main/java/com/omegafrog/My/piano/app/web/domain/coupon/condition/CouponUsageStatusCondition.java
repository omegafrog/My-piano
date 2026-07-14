package com.omegafrog.My.piano.app.web.domain.coupon.condition;

import com.omegafrog.My.piano.app.web.domain.coupon.CouponUsageStatus;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("USAGE_STATUS")
@NoArgsConstructor
public class CouponUsageStatusCondition extends CouponValidationCondition {
    public CouponUsageStatusCondition(int evaluationOrder) { super(evaluationOrder); }
    @Override public CouponValidationResult evaluate(CouponValidationContext context) {
        return context.couponUsageStatus() == CouponUsageStatus.AVAILABLE ? CouponValidationResult.success()
                : CouponValidationResult.failure("USAGE_STATUS", "COUPON_ALREADY_APPLIED_OR_USED");
    }
}
