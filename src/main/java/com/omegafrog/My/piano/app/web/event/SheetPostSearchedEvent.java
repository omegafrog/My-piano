package com.omegafrog.My.piano.app.web.event;

import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SheetPostSearchedEvent extends Event {
  private String rawQuery;
  private String searchedSentence;
  private List<String> instrument;
  private List<String> difficulty;
  private List<String> genres;

  public SheetPostSearchedEvent(String rawQuery, String searchedSentence, List<String> instrument,
      List<String> difficulty,
      List<String> genres) {
    this.rawQuery = rawQuery;
    this.searchedSentence = searchedSentence;
    this.instrument = instrument;
    this.difficulty = difficulty;
    this.genres = genres;
  }

}
