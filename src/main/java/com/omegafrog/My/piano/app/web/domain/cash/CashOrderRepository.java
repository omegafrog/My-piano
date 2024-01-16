package com.omegafrog.My.piano.app.web.domain.cash;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CashOrderRepository extends JpaRepository<CashOrder, String> {
    Optional<CashOrder> findByOrderId(String orderId);
}