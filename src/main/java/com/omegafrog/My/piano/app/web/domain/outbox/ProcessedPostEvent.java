package com.omegafrog.My.piano.app.web.domain.outbox;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "processed_post_event")
@Getter
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
public class ProcessedPostEvent {

    @Id
    private String eventId;

    @Column(nullable = false)
    private Long postId;

    @Column(nullable = false)
    private Long eventVersion;

    @Column(nullable = false)
    private LocalDateTime processedAt;

    private ProcessedPostEvent(String eventId, Long postId, Long eventVersion, LocalDateTime processedAt) {
        this.eventId = eventId;
        this.postId = postId;
        this.eventVersion = eventVersion;
        this.processedAt = processedAt;
    }

    public static ProcessedPostEvent of(String eventId, Long postId, Long eventVersion, LocalDateTime processedAt) {
        return new ProcessedPostEvent(eventId, postId, eventVersion, processedAt);
    }
}
