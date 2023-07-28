package com.omegafrog.My.piano.app.web.domain.order;

import com.omegafrog.My.piano.app.web.domain.coupon.Coupon;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.dto.order.SellableItemDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

@Entity
@Table(name = "`Order`")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @OneToOne
    @JoinColumn(name = "SELLER_ID")
    private User seller;

    @NotNull
    @OneToOne
    @JoinColumn(name = "BUYER_ID")
    private User buyer;

    @NotNull
    @OneToOne
    @JoinColumn(name = "ITEM_ID")
    private SellableItem item;

    @NotNull
    private int initialPrice;

    private Integer totalPrice;

    @Builder.Default
    private Long discountRate=0L;

    @OneToOne
    @JoinColumn(name = "COUPON_ID")
    private Coupon coupon;

    @Builder
    public Order(User seller, User buyer, SellableItem item, int initialPrice, Long discountRate, Coupon coupon) {
        this.seller = seller;
        this.buyer = buyer;
        this.item = item;
        this.initialPrice = initialPrice;
        this.discountRate = discountRate;
        this.coupon = coupon;
    }


    public Order update(Order order){
        this.seller = order.getSeller();
        this.buyer = order.getBuyer();
        this.initialPrice = order.getInitialPrice();
        this.discountRate = order.getDiscountRate();
        this.coupon = order.getCoupon();
        this.totalPrice = order.getTotalPrice();
        return this;
    }



    public void calculateTotalPrice(){
        Long couponDiscountRate = (coupon != null) ? coupon.getDiscountRate() : 0L;
        Long totalDiscountRate = couponDiscountRate + discountRate;
        double tmp = (double) initialPrice*(1-totalDiscountRate);
        totalPrice =  (int) Math.floor(tmp);
    }

    public OrderDto toDto() {
        SellableItemDto dto=null;
        if (item instanceof Lesson lesson) {
            dto =lesson.toDto();
        }else if(item instanceof SheetPost sheetPost) {
            dto = sheetPost.toInfoDto();
        }
        return OrderDto.builder()
                .discountRate(discountRate)
                .buyer(buyer.getUserProfile())
                .seller(buyer.getUserProfile())
                .id(id)
                .item(dto)
                .coupon(coupon)
                .initialPrice(initialPrice)
                .totalPrice(totalPrice)
                .build();
    }
}
