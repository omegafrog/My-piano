package com.omegafrog.My.piano.app.web.domain.coupon;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.exception.payment.ExpiredCouponException;
import com.omegafrog.My.piano.app.web.exception.payment.CouponValidationException;
import com.omegafrog.My.piano.app.web.domain.coupon.condition.CouponValidationCondition;
import com.omegafrog.My.piano.app.web.domain.coupon.condition.CouponValidationContext;
import com.omegafrog.My.piano.app.web.domain.coupon.condition.CouponValidationResult;

import jakarta.persistence.Entity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@NoArgsConstructor
@Getter
@Setter(AccessLevel.PRIVATE)
public class Coupon {

	@Id
	private String id = "coupon-" + UUID.randomUUID();

	@JsonBackReference
	@ManyToOne
	private User owner;

	private String name;
	// 쿠폰 카테고리. 배민 예를 들면 포장전용 같은것
	private int category;
	private Double discountRate;

	private LocalDateTime expireDate;

	private LocalDateTime createdAt = LocalDateTime.now();

	@Embedded
	private DiscountTerms discountTerms;

	@Enumerated(EnumType.STRING)
	private CouponUsageStatus usageStatus = CouponUsageStatus.AVAILABLE;

	@OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<CouponValidationCondition> validationConditions = new ArrayList<>();

	/**
	 *
	 * @param loggedInUser 로그인한 유저
	 * @throws ExpiredCouponException 기한이 만기된 쿠폰일 경우
	 * @throws AccessDeniedException 사용하는 유저가 소유하지 않은 쿠폰인 경우
	 */
	public void validate(User loggedInUser) {
		LocalDateTime now = LocalDateTime.now();
		if (now.isAfter(expireDate))
			throw new ExpiredCouponException("Coupon is expired.");
		if (!owner.equals(loggedInUser))
			throw new AccessDeniedException("Coupon owner is different from logged in user.");
	}

	public static Coupon issue(User owner, String name, DiscountTerms discountTerms,
			List<CouponValidationCondition> validationConditions) {
		Coupon coupon = new Coupon();
		coupon.owner = Objects.requireNonNull(owner, "owner must not be null");
		coupon.name = Objects.requireNonNull(name, "name must not be null");
		coupon.discountTerms = Objects.requireNonNull(discountTerms, "discountTerms must not be null");
		coupon.validationConditions.addAll(Objects.requireNonNull(validationConditions, "validationConditions must not be null"));
		if (coupon.validationConditions.isEmpty()) throw new IllegalArgumentException("validationConditions must not be empty");
		coupon.usageStatus = CouponUsageStatus.AVAILABLE;
		return coupon;
	}

	public CouponValidationResult validateFor(Long applyingUserId, BigDecimal orderAmount, LocalDateTime now) {
		if (owner == null || owner.getId() == null) throw new IllegalStateException("coupon owner must be persisted before validation");
		CouponValidationContext context = new CouponValidationContext(owner.getId(), applyingUserId, orderAmount, now, usageStatus);
		return validationConditions.stream()
			.sorted(Comparator.comparingInt(CouponValidationCondition::getEvaluationOrder))
			.map(condition -> condition.evaluate(context))
			.filter(result -> !result.isSuccess())
			.findFirst()
			.orElseGet(CouponValidationResult::success);
	}

	public BigDecimal temporarilyApply(Long applyingUserId, BigDecimal orderAmount, LocalDateTime now) {
		CouponValidationResult result = validateFor(applyingUserId, orderAmount, now);
		if (!result.isSuccess()) {
			throw new CouponValidationException(result.getFailure().orElseThrow().reasonCode());
		}
		if (discountTerms == null) throw new IllegalStateException("discountTerms must not be null");
		usageStatus = CouponUsageStatus.TEMPORARILY_APPLIED;
		return discountTerms.calculateDiscount(orderAmount);
	}

	public void markUsed() {
		if (usageStatus != CouponUsageStatus.TEMPORARILY_APPLIED) throw new CouponValidationException("COUPON_NOT_TEMPORARILY_APPLIED");
		usageStatus = CouponUsageStatus.USED;
	}

	public void restore() {
		if (usageStatus != CouponUsageStatus.TEMPORARILY_APPLIED) throw new CouponValidationException("COUPON_NOT_TEMPORARILY_APPLIED");
		usageStatus = CouponUsageStatus.AVAILABLE;
	}

	@Builder
	public Coupon(String name, String code, Double discountRate, LocalDateTime expireDate) {
		this.name = name;
		this.discountRate = discountRate;
		this.expireDate = expireDate;
	}
}
