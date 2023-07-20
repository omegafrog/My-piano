package com.omegafrog.My.piano.app.web.infra.order;

import com.omegafrog.My.piano.app.web.domain.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SimpleJpaOrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyer_Id(Long id);
}
