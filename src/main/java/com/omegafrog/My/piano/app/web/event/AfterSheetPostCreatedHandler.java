package com.omegafrog.My.piano.app.web.event;

import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class AfterSheetPostCreatedHandler {

  private final KafkaTemplate kafkaTemplate;

  public AfterSheetPostCreatedHandler(KafkaTemplate kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @TransactionalEventListener
  public void produceEvent(SheetPostCreatedEvent event) {
    kafkaTemplate.send("sheet-post-created-topic", event);
  }
}
