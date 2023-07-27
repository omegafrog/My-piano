package com.omegafrog.My.piano.app.web.dto.order;

import com.omegafrog.My.piano.app.web.domain.coupon.Coupon;
import com.omegafrog.My.piano.app.web.dto.user.UserProfile;
import lombok.*;

@Data
@NoArgsConstructor
@Setter
@Getter
public class OrderDto {
    private Long id;
    private SellableItemDto item;
    private UserProfile seller;
    private UserProfile buyer;
    private int initialPrice;
    private int totalPrice;
    private Long discountRate;
    private Coupon coupon;

    @Builder
    public OrderDto(
            Long id,
            SellableItemDto item,
            UserProfile seller,
            UserProfile buyer,
            Long discountRate,
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
