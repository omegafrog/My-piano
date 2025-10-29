package com.omegafrog.My.piano.app.web.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;

import jakarta.persistence.EntityNotFoundException;

@Component
public class SheetPostCreatedEventConsumer {

  private final ElasticSearchInstance instance;
  private final SheetPostRepository sheetPostRepository;

  public SheetPostCreatedEventConsumer(ElasticSearchInstance instance) {
    this.instance = instance;
    this.sheetPostRepository = null;
  }

  @KafkaListener(topics = "sheet-post-created-topic", groupId = "elasticsearch-consumer")
  public void createInvertIndex(SheetPostCreatedEvent event) {
    try {
      SheetPost sheetPost = sheetPostRepository.findById(event.getSheetPostId())
          .orElseThrow(() -> new EntityNotFoundException());
      instance.invertIndexingSheetPost(sheetPost);

    } catch (EntityNotFoundException e) {
      // TODO : compensation or retry needed
    }

  }
}
