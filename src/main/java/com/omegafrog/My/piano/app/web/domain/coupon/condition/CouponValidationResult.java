package com.omegafrog.My.piano.app.web.domain.coupon.condition;

import java.util.Optional;

public final class CouponValidationResult {
    private static final CouponValidationResult SUCCESS = new CouponValidationResult(null);
    private final CouponValidationFailure failure;

    private CouponValidationResult(CouponValidationFailure failure) {
        this.failure = failure;
    }

    public static CouponValidationResult success() { return SUCCESS; }
    public static CouponValidationResult failure(String conditionType, String reasonCode) {
        return new CouponValidationResult(new CouponValidationFailure(conditionType, reasonCode));
    }
    public boolean isSuccess() { return failure == null; }
    public Optional<CouponValidationFailure> getFailure() { return Optional.ofNullable(failure); }
}
