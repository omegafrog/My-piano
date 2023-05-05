package com.omegafrog.My.piano.app.lesson.entity.enums;

public enum Category {
    SHEET_COMPLETE("한곡 완성법"),
    ACCOMPANIMENT("반주법");

    final String description;

    Category(String description) {
        this.description = description;
    }
}
