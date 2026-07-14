package com.omegafrog.My.piano.app.web.infra.coupon;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponApplication;

public interface SimpleJpaCouponApplicationRepository extends JpaRepository<CouponApplication, String> {
    Optional<CouponApplication> findByOrderId(Long orderId);
}
