package com.omegafrog.My.piano.app.web.domain.coupon;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    @Getter
    private String code;
    // 쿠폰 카테고리. 배민 예를 들면 포장전용 같은것
    private int category;
    @Getter
    private Long discountRate;

    private LocalDateTime expireDate;
    private LocalDateTime created_at;

    @Builder
    public Coupon( String name, String code, Long discountRate) {
        this.name = name;
        this.code = code;
        this.discountRate = discountRate;
        this.created_at = LocalDateTime.now();
    }
}
