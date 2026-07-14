package com.omegafrog.My.piano.app.web.infra.coupon;

import org.springframework.stereotype.Repository;
import com.omegafrog.My.piano.app.web.domain.coupon.ProcessedPaymentEvent;
import com.omegafrog.My.piano.app.web.domain.coupon.ProcessedPaymentEventRepository;
import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class JpaProcessedPaymentEventRepositoryImpl implements ProcessedPaymentEventRepository {
    private final SimpleJpaProcessedPaymentEventRepository jpaRepository;
    public boolean existsByEventId(String eventId) { return jpaRepository.existsById(eventId); }
    public ProcessedPaymentEvent save(ProcessedPaymentEvent processedPaymentEvent) { return jpaRepository.save(processedPaymentEvent); }
}
