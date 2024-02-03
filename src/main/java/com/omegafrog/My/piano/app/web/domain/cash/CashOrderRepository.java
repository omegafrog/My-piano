package com.omegafrog.My.piano.app.web.domain.cash;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CashOrderRepository extends JpaRepository<CashOrder, String>, CashOrderRepositoryCustom {
    Optional<CashOrder> findByOrderId(String orderId);
}