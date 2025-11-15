package com.omegafrog.My.piano.app.web.event;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RetryableCompensationDecorator {

  public static final int MAX_RETRIABLE_COUNT = 5;
  public static final int BACKOFF_SECOND = 2;

  public static void execute(CopmensationCallback callback, Event event) {

    int attempt = 0;
    int backoff = BACKOFF_SECOND;

    while (attempt < MAX_RETRIABLE_COUNT) {
      try {
        callback.execute();
      } catch (Exception e) {
        attempt++;

        if (attempt >= MAX_RETRIABLE_COUNT) {
          log.error("retry failed.");
          throw e;
        }

        try {
          Thread.sleep(backoff * 1000L);
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
        }

        backoff = backoff * 2;

      }
    }
    throw new IllegalArgumentException();
  }
}