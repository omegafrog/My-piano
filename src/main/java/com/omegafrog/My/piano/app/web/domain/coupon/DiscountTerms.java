package com.omegafrog.My.piano.app.web.domain.coupon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class DiscountTerms {
    @Enumerated(EnumType.STRING)
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal maximumDiscountAmount;

    public DiscountTerms(DiscountType discountType, BigDecimal discountValue, BigDecimal maximumDiscountAmount) {
        this.discountType = Objects.requireNonNull(discountType, "discountType must not be null");
        this.discountValue = positive(discountValue, "discountValue");
        this.maximumDiscountAmount = maximumDiscountAmount == null ? null : positive(maximumDiscountAmount, "maximumDiscountAmount");
        if (discountType == DiscountType.PERCENTAGE && discountValue.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new IllegalArgumentException("percentage discount must not exceed 100");
        }
    }

    public BigDecimal calculateDiscount(BigDecimal orderAmount) {
        BigDecimal amount = positive(orderAmount, "orderAmount");
        BigDecimal discount = discountType == DiscountType.FIXED_AMOUNT
                ? discountValue
                : amount.multiply(discountValue).divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
        if (maximumDiscountAmount != null) {
            discount = discount.min(maximumDiscountAmount);
        }
        return discount.min(amount);
    }

    private static BigDecimal positive(BigDecimal value, String field) {
        Objects.requireNonNull(value, field + " must not be null");
        if (value.signum() <= 0) throw new IllegalArgumentException(field + " must be positive");
        return value;
    }
}
