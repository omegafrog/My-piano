package com.omegafrog.My.piano.app.web.service;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.omegafrog.My.piano.app.web.domain.coupon.Coupon;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponRepository;
import com.omegafrog.My.piano.app.web.domain.coupon.DiscountTerms;
import com.omegafrog.My.piano.app.web.domain.coupon.condition.CouponOwnerCondition;
import com.omegafrog.My.piano.app.web.domain.coupon.condition.CouponUsageStatusCondition;
import com.omegafrog.My.piano.app.web.domain.coupon.condition.CouponValidityPeriodCondition;
import com.omegafrog.My.piano.app.web.domain.coupon.condition.MinimumOrderAmountCondition;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.coupon.CouponResponse;
import com.omegafrog.My.piano.app.web.dto.coupon.IssueCouponRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CouponIssuanceApplicationService {
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    @Transactional
    public CouponResponse issue(IssueCouponRequest request) {
        User owner = userRepository.findById(request.userId()).orElseThrow(() -> new IllegalArgumentException("coupon owner not found"));
        BigDecimal minimum = request.minimumOrderAmount() == null ? BigDecimal.ZERO : request.minimumOrderAmount();
        Coupon coupon = Coupon.issue(owner, request.name(), new DiscountTerms(request.discountType(), request.discountValue(), request.maximumDiscountAmount()), List.of(
                new CouponOwnerCondition(0), new CouponValidityPeriodCondition(1, request.validFrom(), request.validUntil()),
                new CouponUsageStatusCondition(2), new MinimumOrderAmountCondition(3, minimum)));
        return CouponResponse.from(couponRepository.save(coupon));
    }
}
