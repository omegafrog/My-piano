package com.omegafrog.My.piano.app.enums;

public enum Position {
    SUPER_ADMIN("슈퍼 어드민"),
    ADMIN("일반 어드민");

    private String description;

    Position(String description) {
        this.description = description;
    }
}
