package com.omegafrog.My.piano.app.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import com.omegafrog.My.piano.app.web.domain.coupon.DiscountType;
import com.omegafrog.My.piano.app.web.dto.coupon.IssueCouponRequest;

class AdminCouponControllerTest {
    @Test void issueRequestRequiresDiscountAndValidity() {
        IssueCouponRequest request = new IssueCouponRequest(1L, "신규 할인", DiscountType.FIXED_AMOUNT, BigDecimal.TEN, BigDecimal.ZERO, null, LocalDateTime.now(), LocalDateTime.now().plusDays(1));
        assertThat(request.name()).isEqualTo("신규 할인");
    }
}
