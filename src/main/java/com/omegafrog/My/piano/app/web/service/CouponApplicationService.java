package com.omegafrog.My.piano.app.web.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.omegafrog.My.piano.app.web.domain.coupon.Coupon;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponApplication;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponApplicationRepository;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponRepository;
import com.omegafrog.My.piano.app.web.domain.order.Order;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponApplicationService {
    private final CouponRepository couponRepository;
    private final CouponApplicationRepository couponApplicationRepository;

    @Transactional
    public void temporarilyApply(Order order, Long applyingUserId) {
        Coupon attachedCoupon = order.getCoupon();
        if (attachedCoupon == null) return;
        Coupon coupon = couponRepository.findById(attachedCoupon.getId()).orElseThrow(() -> new IllegalArgumentException("coupon not found"));
        BigDecimal discount = coupon.temporarilyApply(applyingUserId, BigDecimal.valueOf(order.getInitialPrice()), LocalDateTime.now());
        order.applyCouponDiscount(discount);
        couponApplicationRepository.save(CouponApplication.temporarilyApply(order.getId(), coupon.getId(), discount, LocalDateTime.now()));
    }
}
