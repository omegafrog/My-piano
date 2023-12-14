package com.omegafrog.My.piano.app.web.domain.cash;

import com.omegafrog.My.piano.app.utils.exception.payment.CashOrderCalculateFailureException;
import com.omegafrog.My.piano.app.web.dto.CashOrderDto;
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

    @Nullable
    @Unique
    private String paymentKey;

    private PaymentType paymentType;

    @Positive
    private int amount;

    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime createdAt = LocalDateTime.now();

    private OrderStatus status = OrderStatus.CREATED;

    public CashOrder(String orderId, String paymentKey, PaymentType paymentType, int amount) {
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.paymentType = paymentType;
        this.amount = amount;
    }

    public CashOrder(String orderId, int amount) {
        this.orderId = orderId;
        this.amount = amount;
        this.paymentType = PaymentType.NORMAL;
    }

    public CashOrderDto toDto() {
        return new CashOrderDto(orderId, paymentKey, paymentType, amount, createdAt, status);
    }

    public void validate(int amount) {
        if (this.amount != amount) throw new CashOrderCalculateFailureException("결제 금액이 맞지 않습니다.");

    }

    public CashOrder update(String paymentKey) {
        this.paymentKey = paymentKey;
        return this;
    }

    public void changeStatus(OrderStatus status) {
        this.status = status;
    }
}
