package com.omegafrog.My.piano.app.web.event;

import java.time.LocalDateTime;


import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SheetPostCreatedEvent extends Event {
  private String uploadId;
  private String eventId;
  private LocalDateTime createdAt;
  private Long sheetPostId;

  public SheetPostCreatedEvent(String uploadId, Long sheetPostId) {
    this.uploadId = uploadId;
    this.eventId = java.util.UUID.randomUUID().toString();
    this.createdAt = LocalDateTime.now();
    this.sheetPostId = sheetPostId;
  }
}
