package com.omegafrog.My.piano.app.web.domain.coupon.condition;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "condition_type")
@Getter
@NoArgsConstructor
public abstract class CouponValidationCondition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int evaluationOrder;

    protected CouponValidationCondition(int evaluationOrder) {
        if (evaluationOrder < 0) throw new IllegalArgumentException("evaluationOrder must be non-negative");
        this.evaluationOrder = evaluationOrder;
    }

    public abstract CouponValidationResult evaluate(CouponValidationContext context);
}
