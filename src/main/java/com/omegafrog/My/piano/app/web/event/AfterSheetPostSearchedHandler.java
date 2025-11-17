package com.omegafrog.My.piano.app.web.event;

import java.io.IOException;
import java.util.List;

import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
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
