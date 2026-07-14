package com.omegafrog.My.piano.app.web.infra.coupon;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponApplication;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponApplicationRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaCouponApplicationRepositoryImpl implements CouponApplicationRepository {
    private final SimpleJpaCouponApplicationRepository jpaRepository;
    public CouponApplication save(CouponApplication couponApplication) { return jpaRepository.save(couponApplication); }
    public Optional<CouponApplication> findByOrderId(Long orderId) { return jpaRepository.findByOrderId(orderId); }
}
