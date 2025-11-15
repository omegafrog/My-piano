package com.omegafrog.My.piano.app.web.event;

import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class SheetPostSearchedEvent extends Event {
  private final String searchedSentence;
  private final List<String> instrument;
  private final List<String> difficulty;
  private final List<String> genres;
}
