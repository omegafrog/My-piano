package com.omegafrog.My.piano.app.web.dto.coupon;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import com.omegafrog.My.piano.app.web.domain.coupon.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;

public record IssueCouponRequest(@NotNull Long userId, @NotBlank String name, @NotNull DiscountType discountType,
        @NotNull @Positive BigDecimal discountValue, @PositiveOrZero BigDecimal minimumOrderAmount,
        @Positive BigDecimal maximumDiscountAmount, @NotNull LocalDateTime validFrom, @NotNull LocalDateTime validUntil) { }
