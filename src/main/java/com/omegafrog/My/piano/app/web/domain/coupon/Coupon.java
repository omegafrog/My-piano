package com.omegafrog.My.piano.app.web.domain.coupon;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.security.access.AccessDeniedException;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.exception.payment.ExpiredCouponException;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
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

	@Builder
	public Coupon(String name, String code, Double discountRate, LocalDateTime expireDate) {
		this.name = name;
		this.discountRate = discountRate;
		this.expireDate = expireDate;
	}
}
