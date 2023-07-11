package com.omegafrog.My.piano.app.sheet.dto;

import com.omegafrog.My.piano.app.coupon.entity.Coupon;
import com.omegafrog.My.piano.app.order.entity.Item;
import com.omegafrog.My.piano.app.user.entity.User;
import lombok.*;

import javax.persistence.*;

@Data
@NoArgsConstructor
@Setter
@Getter
public class OrderDto {
    private Long id;
    private User seller;
    private User buyer;
    private Item item;
    private Long price;
    private Long discountRate;
    private Coupon coupon;

    @Builder
    public OrderDto(Long id, User seller, User buyer, Item item, Long price, Long discountRate, Coupon coupon) {
        this.id = id;
        this.seller = seller;
        this.buyer = buyer;
        this.item = item;
        this.price = price;
        this.discountRate = discountRate;
        this.coupon = coupon;
    }
}
