package com.omegafrog.My.piano.app.web.service.outbox;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.omegafrog.My.piano.app.web.domain.outbox.PostOutboxEvent;
import com.omegafrog.My.piano.app.web.domain.outbox.PostOutboxEventRepository;
import com.omegafrog.My.piano.app.web.domain.outbox.PostOutboxEventType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PostOutboxService {

    private final PostOutboxEventRepository postOutboxEventRepository;

    @Transactional
    public void enqueue(PostOutboxEventType eventType, Long postId, Long eventVersion) {
        String eventId = UUID.randomUUID().toString();
        PostOutboxEvent event = PostOutboxEvent.pending(eventId, postId, eventVersion, eventType);
        postOutboxEventRepository.save(event);
    }
}
