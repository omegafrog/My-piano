package com.omegafrog.My.piano.app.enums;

public enum Difficulty {
    VERY_EASY("매우 쉬움"),
    EASY("쉬움"),
    MEDIUM("보통"),
    HARD("어려움"),
    VERY_HARD("매우 어려움");

    final String description;

    Difficulty(String description) {
        this.description = description;
    }

}
