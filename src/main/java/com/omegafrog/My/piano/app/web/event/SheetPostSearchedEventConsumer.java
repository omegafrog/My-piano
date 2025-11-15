package com.omegafrog.My.piano.app.web.event;

import org.springframework.kafka.annotation.KafkaListener;

public class SheetPostSearchedEventConsumer {
  
  @KafkaListener(topics = "sheet-post-searched-topic", groupId = "elasticsearch-consumer")
  public void indexSearchLog(SheetPostSearchedEvent event){
    
  }
}
