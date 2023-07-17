package com.omegafrog.My.piano.app.web.infra.coupon;

import com.omegafrog.My.piano.app.web.domain.coupon.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SimpleJpaCouponRepository extends JpaRepository<Coupon, Long> {
}
