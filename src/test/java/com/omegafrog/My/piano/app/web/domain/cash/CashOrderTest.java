package com.omegafrog.My.piano.app.web.domain.cash;

import com.omegafrog.My.piano.app.web.exception.payment.CashOrderCalculateFailureException;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.enums.OrderStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;


class CashOrderTest {

    @Test
    void validateTest() {
        int amount = 10000;
        CashOrder cashOrder = new CashOrder("cash-" + UUID.randomUUID(), "test name", amount, new User());
        int wrongAmount = 9000;
        Assertions.assertThrows(CashOrderCalculateFailureException.class, () -> cashOrder.validate(wrongAmount));
    }


    @Test
    void update() {
        int amount = 10000;
        String paymentkey = UUID.randomUUID().toString();
        CashOrder cashOrder = new CashOrder("cash-" + UUID.randomUUID(), "test name", amount, new User());
        cashOrder.update(paymentkey);
        Assertions.assertEquals(paymentkey, cashOrder.getPaymentKey());
    }


    @Test
    void changeState() {
        int amount = 10000;
        CashOrder cashOrder = new CashOrder("cash-" + UUID.randomUUID(), "test name", amount, new User());
        cashOrder.changeState(OrderStatus.DONE);
        Assertions.assertEquals(OrderStatus.DONE, cashOrder.getStatus());
    }

    @Test
    void expire() {
        int amount = 10000;
        CashOrder cashOrder = new CashOrder("cash-" + UUID.randomUUID(), "test name", amount, new User());
        cashOrder.expire();
        Assertions.assertEquals(OrderStatus.EXPIRED, cashOrder.getStatus());
    }
}