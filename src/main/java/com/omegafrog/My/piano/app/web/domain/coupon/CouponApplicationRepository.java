package com.omegafrog.My.piano.app.web.domain.coupon;

import java.util.Optional;

public interface CouponApplicationRepository {
    CouponApplication save(CouponApplication couponApplication);
    Optional<CouponApplication> findByOrderId(Long orderId);
}
