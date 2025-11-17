package com.omegafrog.My.piano.app.external.elasticsearch;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.elasticsearch.annotations.Document;

import jakarta.persistence.Id;
import lombok.Getter;

@Getter
@Document(indexName = "sheetpost-search")
public class SheetPostSearchIndex {
  @Id
  private final String id;
  private final String rawQuery;
  private final List<String> tokens;
  private final Instant timestamp;

  public SheetPostSearchIndex(String rawQuery, List<String> tokens, Instant timestamp) {
    this.id = UUID.randomUUID().toString();
    this.rawQuery = rawQuery;
    this.tokens = tokens;
    this.timestamp = timestamp;
  }

}
