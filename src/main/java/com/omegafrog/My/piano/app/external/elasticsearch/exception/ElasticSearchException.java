package com.omegafrog.My.piano.app.external.elasticsearch.exception;

public class ElasticSearchException extends RuntimeException {
  public ElasticSearchException(String message, Throwable e) {
    super(message, e);
  }
}
