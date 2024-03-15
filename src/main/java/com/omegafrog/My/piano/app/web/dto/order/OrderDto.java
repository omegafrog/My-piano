package com.omegafrog.My.piano.app.web.dto.order;

import com.omegafrog.My.piano.app.web.domain.coupon.Coupon;
import com.omegafrog.My.piano.app.web.dto.user.UserInfo;
import lombok.*;

@Data
@NoArgsConstructor
@Setter
@Getter
public class OrderDto {
    private Long id;
    private SellableItemDto item;
    private UserInfo seller;
    private UserInfo buyer;
    private int initialPrice;
    private int totalPrice;
    private Double discountRate;
    private Coupon coupon;

    @Builder
    public OrderDto(
            Long id,
            SellableItemDto item,
            UserInfo seller,
            UserInfo buyer,
            Double discountRate,
            Coupon coupon,
            int initialPrice,
            int totalPrice) {
        this.id = id;
        this.seller = seller;
        this.buyer = buyer;
        this.item = item;
        this.initialPrice = initialPrice;
        this.totalPrice = totalPrice;
        this.discountRate = discountRate;
        this.coupon = coupon;
    }
}
