package com.omegafrog.My.piano.app.order.entity;

import com.omegafrog.My.piano.app.coupon.entity.Coupon;
import com.omegafrog.My.piano.app.sheet.dto.OrderDto;
import com.omegafrog.My.piano.app.user.entity.User;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "SELLER_ID")
    private User seller;

    @OneToOne
    @JoinColumn(name = "BUYER_ID")
    private User buyer;

    @OneToOne
    @JoinColumn(name = "ITEM_ID")
    private Item item;

    private Long price;

    private Long discountRate;

    @OneToOne
    @JoinColumn(name = "COUPON_ID")
    private Coupon coupon;

    @Builder
    public Order(User seller, User buyer, Item item) {
        this.seller = seller;
        this.buyer = buyer;
        this.item = item;
    }

    public Long calculateTotalPrice(){
        price = price * (1-discountRate);
        price = price * (1-coupon.getDiscountRate());
        return price;
    }

    public OrderDto toDto(){
        return OrderDto.builder()
                .discountRate(discountRate)
                .buyer(buyer)
                .seller(seller)
                .id(id)
                .item(item)
                .coupon(coupon)
                .price(price)
                .build();
    }
}
