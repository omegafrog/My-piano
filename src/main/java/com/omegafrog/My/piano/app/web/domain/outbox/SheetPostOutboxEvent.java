package com.omegafrog.My.piano.app.web.domain.outbox;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "sheet_post_outbox_event", indexes = {
        @Index(name = "idx_sheet_outbox_status_next_attempt", columnList = "status,nextAttemptAt")
})
@Getter
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
public class SheetPostOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;

    @Column(nullable = false)
    private Long sheetPostId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SheetPostOutboxEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SheetPostOutboxEventStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(length = 500)
    private String lastError;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private SheetPostOutboxEvent(String eventId, Long sheetPostId, SheetPostOutboxEventType eventType) {
        this.eventId = eventId;
        this.sheetPostId = sheetPostId;
        this.eventType = eventType;
        this.status = SheetPostOutboxEventStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now();
        this.nextAttemptAt = this.createdAt;
    }

    public static SheetPostOutboxEvent pending(String eventId, Long sheetPostId, SheetPostOutboxEventType eventType) {
        return new SheetPostOutboxEvent(eventId, sheetPostId, eventType);
    }

    public void markCompleted(LocalDateTime now) {
        this.status = SheetPostOutboxEventStatus.COMPLETED;
        this.processedAt = now;
        this.lastError = null;
    }

    public void markFailed(String errorMessage, LocalDateTime nextAttemptAt) {
        this.status = SheetPostOutboxEventStatus.FAILED;
        this.retryCount += 1;
        this.lastError = errorMessage;
        this.nextAttemptAt = nextAttemptAt;
    }
}
