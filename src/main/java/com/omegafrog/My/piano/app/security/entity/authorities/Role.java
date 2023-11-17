package com.omegafrog.My.piano.app.security.entity.authorities;

public enum Role {
    ADMIN("ADMIN","관리자"),
    CREATOR("CREATOR","크리에이터"),
    USER("USER","일반 사용자");
    public final String value;
    public final String description;

    Role(String value, String description) {
        this.value = value;
        this.description = description;
    }
}
