package com.omegafrog.My.piano.app.web.event;

import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;

public class AfterSheetPostSearchedHandler {
  private final KafkaTemplate kafkaTemplate;

  public AfterSheetPostSearchedHandler(KafkaTemplate kafkaTemplate) {
    this.kafkaTemplate = kafkaTemplate;
  }

  @EventListener(classes = SheetPostSearchedEvent.class)
  public void publishEvent(SheetPostSearchedEvent event) {
    kafkaTemplate.send("sheet-post-searched-topic", event);
  }
}
