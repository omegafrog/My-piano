package com.omegafrog.My.piano.app.security.entity;

public enum Role {
    ADMIN("관리자"),
    SUPER_ADMIN("슈퍼 관리자"),
    USER("일반 사용자");
    final String description;

    Role(String description) {
        this.description = description;
    }
}
