package com.omegafrog.My.piano.app.web.domain.coupon;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "coupon_application")
@Getter
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
public class CouponApplication {
    @Id
    private String id;

    @Column(nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false)
    private String couponId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal discountAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponApplicationStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private CouponApplication(Long orderId, String couponId, BigDecimal discountAmount, LocalDateTime createdAt) {
        this.id = "coupon-application-" + UUID.randomUUID();
        this.orderId = Objects.requireNonNull(orderId, "orderId must not be null");
        this.couponId = Objects.requireNonNull(couponId, "couponId must not be null");
        this.discountAmount = Objects.requireNonNull(discountAmount, "discountAmount must not be null");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt must not be null");
        this.status = CouponApplicationStatus.TEMPORARILY_APPLIED;
    }

    public static CouponApplication temporarilyApply(Long orderId, String couponId, BigDecimal discountAmount, LocalDateTime createdAt) {
        return new CouponApplication(orderId, couponId, discountAmount, createdAt);
    }

    public void markUsed() {
        if (status != CouponApplicationStatus.TEMPORARILY_APPLIED) throw new IllegalStateException("coupon application is not temporarily applied");
        status = CouponApplicationStatus.USED;
    }

    public void restore() {
        if (status != CouponApplicationStatus.TEMPORARILY_APPLIED) throw new IllegalStateException("coupon application is not temporarily applied");
        status = CouponApplicationStatus.RESTORED;
    }
}
