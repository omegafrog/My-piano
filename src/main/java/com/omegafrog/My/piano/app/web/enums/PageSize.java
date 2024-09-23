package com.omegafrog.My.piano.app.web.enums;

public enum PageSize {
    SMALL(30),
    MEDIUM(50),
    LARGE(100);

    public int size;
    PageSize(int size) {
        this.size = size;
    }
}
