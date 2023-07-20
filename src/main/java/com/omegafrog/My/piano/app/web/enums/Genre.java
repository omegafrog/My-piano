package com.omegafrog.My.piano.app.web.enums;


public enum Genre {
    CAROL("캐롤"),
    K_POP("K-pop"),
    POP("해외 POP"),
    NEW_AGE("뉴에이지"),
    CLASSIC("클래식"),
    CUSTOM("자작곡"),
    JAZZ("재즈"),
    DUET("연탄곡"),
    GAME_ANIME("게임/애니"),
    OST("OST"),
    BGM("BGM"),
    KIDS("동요"),
    MUSICAL("뮤지컬"),
    RELIGIOUS("종교");
    final String description;

    Genre(String description) {
        this.description = description;
    }
}
