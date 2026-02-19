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
@Table(name = "post_index_version")
@Getter
@NoArgsConstructor
@Setter(AccessLevel.PRIVATE)
public class PostIndexVersion {

    @Id
    private Long postId;

    @Column(nullable = false)
    private Long lastAppliedVersion;

    @Column(nullable = false)
    private String lastEventId;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public PostIndexVersion(Long postId, Long lastAppliedVersion, String lastEventId, LocalDateTime updatedAt) {
        this.postId = postId;
        this.lastAppliedVersion = lastAppliedVersion;
        this.lastEventId = lastEventId;
        this.updatedAt = updatedAt;
    }

    public static PostIndexVersion initialize(Long postId) {
        return new PostIndexVersion(postId, -1L, "", LocalDateTime.now());
    }

    public boolean isStale(Long eventVersion) {
        return eventVersion <= lastAppliedVersion;
    }

    public void apply(Long eventVersion, String eventId, LocalDateTime now) {
        this.lastAppliedVersion = eventVersion;
        this.lastEventId = eventId;
        this.updatedAt = now;
    }
}
