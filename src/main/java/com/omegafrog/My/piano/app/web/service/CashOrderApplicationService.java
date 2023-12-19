package com.omegafrog.My.piano.app.web.service;

import com.omegafrog.My.piano.app.external.tossPayment.TossPaymentInstance;
import com.omegafrog.My.piano.app.web.domain.cash.CashOrder;
import com.omegafrog.My.piano.app.web.domain.cash.CashOrderRepository;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.domain.user.UserRepository;
import com.omegafrog.My.piano.app.web.dto.CashOrderDto;
import com.omegafrog.My.piano.app.web.enums.OrderStatus;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class CashOrderApplicationService {

    private final CashOrderRepository cashOrderRepository;

    private final UserRepository userRepository;

    private final TransactionTemplate transactionTemplate;


    @Autowired
    private TossPaymentInstance paymentInstance;

    @Transactional
    public CashOrderDto createCashOrder(String orderId, int amount){
        CashOrder cashOrder = new CashOrder(orderId, amount);
        CashOrder saved = cashOrderRepository.save(cashOrder);
        return saved.toDto();
    }

    private void beforeRequestCashOrder(String orderId){
        CashOrder byOrderId = cashOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find cash order. id:" + orderId));
        byOrderId.changeStatus(OrderStatus.PROGRESSING);
    }

    @Transactional
    private void invoke(String paymentKey, String orderId, int amount, User loggedInUser){
        CashOrder byOrderId = cashOrderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Cannot find cash order. id:" + orderId));
        byOrderId.validate(amount);
        byOrderId.update(paymentKey);
        paymentInstance.requestCashOrder(byOrderId);
        User user = userRepository.findById(loggedInUser.getId())
                .orElseThrow(() -> new EntityNotFoundException("Cannot find user entity. id:" + loggedInUser.getId()));
        user.addCash(amount);

        byOrderId.changeStatus(OrderStatus.FINISHED);
    }
    /**
     * 결제 승인
     * @param paymentKey
     * @param orderId
     * @param amount
     * @param loggedInUser
     */
    public void requestCashOrder(String paymentKey, String orderId, int amount, User loggedInUser) {
        beforeRequestCashOrder(orderId);
        invoke(paymentKey, orderId, amount, loggedInUser);
    }
}
