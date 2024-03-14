package com.omegafrog.My.piano.app.web.domain.cash;

import com.omegafrog.My.piano.app.web.enums.OrderStatus;
import com.omegafrog.My.piano.app.web.enums.PaymentType;

import java.time.LocalDateTime;

public record PaymentHistory(String paymentKey, String orderId, PaymentType paymentType, String orderName, int amount,
                             LocalDateTime createdAt, OrderStatus status) {
    public PaymentHistory(CashOrder cashOrder){
        this(cashOrder.getPaymentKey(), cashOrder.getOrderId(), cashOrder.getPaymentType(),
                cashOrder.getOrderName(), cashOrder.getAmount(), cashOrder.getCreatedAt(), cashOrder.getStatus());
    }
}
