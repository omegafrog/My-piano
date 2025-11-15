package com.omegafrog.My.piano.app.web.event;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Getter;

@Getter
abstract class Event {
  private final LocalDateTime createdTime;
  private final String eventId;

  public Event() {
    this.createdTime = LocalDateTime.now();
    this.eventId = UUID.randomUUID().toString();
  }
}