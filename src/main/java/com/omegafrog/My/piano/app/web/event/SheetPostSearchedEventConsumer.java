package com.omegafrog.My.piano.app.web.event;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostSearchIndex;
import com.omegafrog.My.piano.app.external.elasticsearch.SheetPostSearchIndexRepository;
import com.omegafrog.My.piano.app.external.elasticsearch.exception.ElasticSearchException;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.AnalyzeResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RequiredArgsConstructor
@Slf4j
@Component
public class SheetPostSearchedEventConsumer {

  private final ElasticsearchClient client;

  private final SheetPostSearchIndexRepository sheetPostSearchIndexRepository;

  private final static String SHEET_POST_INDEX_NAME = "sheetpost";

  @KafkaListener(topics = "sheet-post-searched-topic", groupId = "elasticsearch-consumer")
  public void indexSearchLog(SheetPostSearchedEvent event) {
    try {
      List<String> tokens = tokenizeSearchSentence(event.getSearchedSentence());
      SheetPostSearchIndex sheetPostSearchIndex = new SheetPostSearchIndex(event.getRawQuery(), tokens,
          Instant.now());
      sheetPostSearchIndexRepository.save(sheetPostSearchIndex);
    } catch (ElasticSearchException e) {
      log.error("elasticsearch error", e);
    }

  }

  public List<String> tokenizeSearchSentence(String searchSentence) {
    try {
      AnalyzeResponse response = client.indices()
          .analyze(a -> a.index(SHEET_POST_INDEX_NAME)
              .analyzer("my_nori_analyzer")
              .text(searchSentence));
      return response.tokens().stream().map(token -> token.token()).toList();

    } catch (IOException e) {
      log.error("Failed to analyze search sentence. sentence:{}", searchSentence);
      throw new ElasticSearchException("Failed to analyze search sentence. sentence:" + searchSentence, e);
    }
  }
}
