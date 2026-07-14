package com.omegafrog.My.piano.app.web.domain.coupon.condition;

import java.util.Objects;

public record CouponValidationFailure(String conditionType, String reasonCode) {
    public CouponValidationFailure {
        Objects.requireNonNull(conditionType, "conditionType must not be null");
        Objects.requireNonNull(reasonCode, "reasonCode must not be null");
    }
}
