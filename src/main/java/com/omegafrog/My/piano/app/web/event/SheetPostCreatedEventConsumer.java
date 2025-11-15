package com.omegafrog.My.piano.app.web.event;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.omegafrog.My.piano.app.external.elasticsearch.ElasticSearchInstance;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPostRepository;

import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import jakarta.persistence.EntityNotFoundException;

@Component
public class SheetPostCreatedEventConsumer {

  private final ElasticSearchInstance instance;
  private final SheetPostRepository sheetPostRepository;

  public SheetPostCreatedEventConsumer(ElasticSearchInstance instance, SheetPostRepository sheetPostRepository) {
    this.instance = instance;
    this.sheetPostRepository = sheetPostRepository;
  }

  @Retryable(value = {
      ElasticsearchException.class }, maxAttempts = 5, backoff = @Backoff(delay = 2000, multiplier = 2))
  @KafkaListener(topics = "sheet-post-created-topic", groupId = "elasticsearch-consumer")
  public void createInvertIndex(SheetPostCreatedEvent event) {
    try {
      SheetPost sheetPost = sheetPostRepository.findById(event.getSheetPostId())
          .orElseThrow(() -> new EntityNotFoundException());
      instance.invertIndexingSheetPost(sheetPost);
    } catch (RuntimeException e) {
      throw e;
    }

  }
}