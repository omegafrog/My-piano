package com.omegafrog.My.piano.app.web.controller;

import com.omegafrog.My.piano.app.web.domain.sheet.Genres;
import com.omegafrog.My.piano.app.web.domain.sheet.Sheet;
import com.omegafrog.My.piano.app.web.domain.sheet.SheetPost;
import com.omegafrog.My.piano.app.web.domain.user.User;
import com.omegafrog.My.piano.app.web.enums.Difficulty;
import com.omegafrog.My.piano.app.web.enums.Genre;
import com.omegafrog.My.piano.app.web.enums.Instrument;

public class DummyData {
    public static SheetPost sheetPost(User artist) {
        return SheetPost.builder()
                .sheet(Sheet.builder()
                        .title("title")
                        .sheetUrl("path1")
                        .genres(Genres.builder().genre1(Genre.BGM).build())
                        .user(artist)
                        .difficulty(Difficulty.MEDIUM)
                        .instrument(Instrument.GUITAR_ACOUSTIC)
                        .isSolo(true)
                        .lyrics(false)
                        .pageNum(3)
                        .build())
                .title("SheetPostTitle1")
                .price(12000)
                .artist(artist)
                .content("hihi this is content")
                .build();
    }

}
