package com.omegafrog.My.piano.app.web.domain.cash;

import com.omegafrog.My.piano.app.utils.exception.payment.CashOrderCalculateFailureException;
import com.omegafrog.My.piano.app.web.domain.user.User;
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

}