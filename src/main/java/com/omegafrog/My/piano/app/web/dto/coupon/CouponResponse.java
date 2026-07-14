package com.omegafrog.My.piano.app.web.dto.coupon;

import java.math.BigDecimal;
import com.omegafrog.My.piano.app.web.domain.coupon.Coupon;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponUsageStatus;

public record CouponResponse(String id, Long userId, String name, CouponUsageStatus status, BigDecimal discountValue) {
    public static CouponResponse from(Coupon coupon) {
        return new CouponResponse(coupon.getId(), coupon.getOwner().getId(), coupon.getName(), coupon.getUsageStatus(), coupon.getDiscountTerms().getDiscountValue());
    }
}
