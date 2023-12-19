package com.omegafrog.My.piano.app.web.domain.coupon;

import java.util.Optional;

public interface CouponRepository {
    Coupon save(Coupon coupon);

    Optional<Coupon> findById(String id);

    void deleteById(String id);
}
