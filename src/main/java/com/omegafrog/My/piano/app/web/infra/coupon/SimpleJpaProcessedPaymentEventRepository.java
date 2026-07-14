package com.omegafrog.My.piano.app.web.infra.coupon;

import org.springframework.data.jpa.repository.JpaRepository;
import com.omegafrog.My.piano.app.web.domain.coupon.ProcessedPaymentEvent;

public interface SimpleJpaProcessedPaymentEventRepository extends JpaRepository<ProcessedPaymentEvent, String> { }
