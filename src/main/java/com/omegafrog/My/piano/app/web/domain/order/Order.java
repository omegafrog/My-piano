package com.omegafrog.My.piano.app.web.domain.order;

import com.omegafrog.My.piano.app.web.domain.coupon.Coupon;
import com.omegafrog.My.piano.app.web.domain.lesson.Lesson;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.enums.OrderStatus;
import com.omegafrog.My.piano.app.web.dto.order.SellableItemDto;
import com.omegafrog.My.piano.app.web.dto.order.OrderDto;
import com.omegafrog.My.piano.app.web.domain.user.User;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;

import javax.annotation.Nullable;
import java.io.Serializable;

@Entity
@Table(name = "orders")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class Order implements Serializable {

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

    @OneToOne
    @JoinColumn(name = "ITEM_ID")
    private SellableItem item;

    @NotNull
    private Integer initialPrice;

    private Integer totalPrice;

    @Builder.Default
    private OrderStatus orderStatus=OrderStatus.READY;

    @Builder.Default
    private Double discountRate=0d;

    @OneToOne
    @JoinColumn(name = "COUPON_ID")
    @Nullable
    private Coupon coupon;

    public Order(User seller, User buyer, SellableItem item, Double discountRate, @Nullable Coupon coupon) {
        this.seller = seller;
        this.buyer = buyer;
        this.item = item;
        this.initialPrice = item.getPrice();
        this.discountRate = discountRate;
        this.coupon = coupon;
        this.calculateTotalPrice();
    }

    public void setStatus(OrderStatus status){
        this.orderStatus = status;
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
        Double couponDiscountRate = (coupon != null) ? coupon.getDiscountRate() : 0f;
        Double totalDiscountRate = couponDiscountRate + discountRate;
        double tmp = (double) initialPrice*(1-totalDiscountRate);
        totalPrice =  (int) Math.floor(tmp);
    }

    public OrderDto toDto() {
        SellableItemDto dto=null;
        if (item instanceof Lesson lesson) {
            dto =lesson.toDto();
        }else if(item instanceof SheetPost sheetPost) {
            dto = sheetPost.toDto();
        }
        return OrderDto.builder()
                .discountRate(discountRate)
                .buyer(buyer.getUserInfo())
                .seller(seller.getUserInfo())
                .id(id)
                .item(dto)
                .coupon(coupon)
                .initialPrice(initialPrice)
                .totalPrice(totalPrice)
                .build();
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", seller=" + seller +
                ", buyer=" + buyer +
                ", item=" + item +
                ", initialPrice=" + initialPrice +
                ", totalPrice=" + totalPrice +
                ", orderStatus=" + orderStatus +
                ", discountRate=" + discountRate +
                ", coupon=" + coupon +
                '}';
    }
}
