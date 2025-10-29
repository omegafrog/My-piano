package com.omegafrog.My.piano.app.web.event;

import java.time.LocalDateTime;

import lombok.Getter;

@Getter
public class SheetPostCreatedEvent {
  private String eventId;
  private LocalDateTime createdAt;
  private Long sheetPostId;

  public SheetPostCreatedEvent(Long sheetPostId) {
    this.eventId = java.util.UUID.randomUUID().toString();
    this.createdAt = LocalDateTime.now();
    this.sheetPostId = sheetPostId;
  }
}
