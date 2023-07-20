package com.omegafrog.My.piano.app.web.enums;

public enum Instrument {
    PIANO_KEY_88("88키"),
    PIANO_KEY_61("61키"),

    GUITAR_ACOUSTIC("어쿠스틱"),
    GUITAR_ELECTRIC("일렉트릭"),
    GUITAR_BASE("베이스"),
    GUITAR_UKULELE("우쿨렐레"),

    STRING_VIOLIN("바이올린"),
    STRING_VIOLA("비올라"),
    STRING_CELLO("첼로"),
    STRING_BASE("베이스"),

    WOODWIND_FLUTE("플루트"),
    WOODWIND_PICCOLO("피콜로"),
    WOODWIND_OBOE("오보에");
    final String description;

    Instrument(String description) {
        this.description = description;
    }
}
