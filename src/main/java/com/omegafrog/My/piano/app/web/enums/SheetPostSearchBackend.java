package com.omegafrog.My.piano.app.web.enums;

import java.util.Locale;

public enum SheetPostSearchBackend {
    ES,
    DB;

    public static SheetPostSearchBackend from(String value) {
        if (value == null || value.isBlank()) {
            return ES;
        }

        return SheetPostSearchBackend.valueOf(value.trim().toUpperCase(Locale.ROOT));
    }
}
