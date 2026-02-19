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
@Table(name = "post_outbox_event", indexes = {
        @Index(name = "idx_post_outbox_status_next_attempt", columnList = "status,nextAttemptAt")
})
@Getter
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
public class PostOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String eventId;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long eventVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostOutboxEventType eventType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostOutboxEventStatus status;

    @Column(nullable = false)
    private int retryCount;

    @Column(nullable = false)
    private LocalDateTime nextAttemptAt;

    @Column(length = 500)
    private String lastError;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private PostOutboxEvent(String eventId, Long postId, Long eventVersion, PostOutboxEventType eventType) {
        this.eventId = eventId;
        this.postId = postId;
        this.eventVersion = eventVersion;
        this.eventType = eventType;
        this.status = PostOutboxEventStatus.PENDING;
        this.retryCount = 0;
        this.createdAt = LocalDateTime.now();
        this.nextAttemptAt = this.createdAt;
    }

    public static PostOutboxEvent pending(String eventId, Long postId, Long eventVersion, PostOutboxEventType eventType) {
        return new PostOutboxEvent(eventId, postId, eventVersion, eventType);
    }

    public void markCompleted(LocalDateTime now) {
        this.status = PostOutboxEventStatus.COMPLETED;
        this.processedAt = now;
        this.lastError = null;
    }

    public void markFailed(String errorMessage, LocalDateTime nextAttemptAt) {
        this.status = PostOutboxEventStatus.FAILED;
        this.retryCount += 1;
        this.lastError = errorMessage;
        this.nextAttemptAt = nextAttemptAt;
    }
}
