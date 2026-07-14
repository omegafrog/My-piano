package com.omegafrog.My.piano.app.web.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;
import com.omegafrog.My.piano.app.web.domain.coupon.CouponApplication;

class CouponApplicationServiceTest {
    @Test void couponApplicationNeedsPersistedOrderId() {
        assertThatThrownBy(() -> CouponApplication.temporarilyApply(null, "coupon", java.math.BigDecimal.ONE, java.time.LocalDateTime.now()))
            .isInstanceOf(NullPointerException.class);
    }
}
