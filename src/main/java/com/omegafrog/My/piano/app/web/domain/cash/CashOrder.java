package com.omegafrog.My.piano.app.web.domain.cash;

import com.omegafrog.My.piano.app.utils.exception.payment.CashOrderCalculateFailureException;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.dto.order.CashOrderDto;
import com.omegafrog.My.piano.app.web.enums.OrderStatus;
import com.omegafrog.My.piano.app.web.enums.PaymentType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.checkerframework.common.aliasing.qual.Unique;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

@Entity(name = "cash_order")
@NoArgsConstructor
@Getter
public class CashOrder {
    @Id
    private String orderId;

    private String orderName;

    @Nullable
    @Unique
    private String paymentKey;

    private PaymentType paymentType;

    @Positive
    private int amount;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt = LocalDateTime.now();

    private OrderStatus status = OrderStatus.READY;

    private Long userId;

    public CashOrder(String orderId, String paymentKey, PaymentType paymentType, int amount, User user) {
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.paymentType = paymentType;
        this.amount = amount;
        this.userId = user.getId();
    }

    public CashOrder(String orderId, String orderName, int amount, User user) {
        this.orderName = orderName;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentType = PaymentType.NORMAL;
        this.userId = user.getId();

    }

    public CashOrderDto toDto() {
        return new CashOrderDto(orderId, paymentKey, paymentType, amount, createdAt, status);
    }

    public void validate(int amount) throws CashOrderCalculateFailureException {
        if (this.amount != amount) throw new CashOrderCalculateFailureException("결제 금액이 맞지 않습니다.");

    }

    public CashOrder update(String paymentKey) {
        this.paymentKey = paymentKey;
        return this;
    }

    public void changeState(OrderStatus status) {
        this.status = status;
    }

    public void expire(){
        this.status = OrderStatus.EXPIRED;
    }
}
