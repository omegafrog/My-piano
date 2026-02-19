package com.omegafrog.My.piano.app.web.service.outbox;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.omegafrog.My.piano.app.web.domain.outbox.SheetPostOutboxEvent;
import com.omegafrog.My.piano.app.web.domain.outbox.SheetPostOutboxEventRepository;
import com.omegafrog.My.piano.app.web.domain.outbox.SheetPostOutboxEventType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SheetPostOutboxService {

    private final SheetPostOutboxEventRepository sheetPostOutboxEventRepository;

    @Transactional
    public void enqueueCreated(Long sheetPostId) {
        String eventId = UUID.randomUUID().toString();
        SheetPostOutboxEvent event = SheetPostOutboxEvent.pending(
                eventId,
                sheetPostId,
                SheetPostOutboxEventType.SHEET_POST_CREATED);
        sheetPostOutboxEventRepository.save(event);
    }
}
