package com.omegafrog.My.piano.app.web.domain.coupon;

import java.time.LocalDateTime;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "processed_payment_event")
@Getter
@NoArgsConstructor
public class ProcessedPaymentEvent {
    @Id
    private String eventId;
    @Column(nullable = false)
    private LocalDateTime processedAt;

    private ProcessedPaymentEvent(String eventId, LocalDateTime processedAt) {
        this.eventId = Objects.requireNonNull(eventId, "eventId must not be null");
        this.processedAt = Objects.requireNonNull(processedAt, "processedAt must not be null");
    }
    public static ProcessedPaymentEvent of(String eventId, LocalDateTime processedAt) {
        return new ProcessedPaymentEvent(eventId, processedAt);
    }
}
