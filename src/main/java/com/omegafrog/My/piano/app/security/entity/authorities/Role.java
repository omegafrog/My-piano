package com.omegafrog.My.piano.app.security.entity.authorities;

public enum Role {
    ADMIN("ADMIN","관리자"),
    SUPER_ADMIN("SUPER_ADMIN","슈퍼 관리자"),
    USER("USER","일반 사용자");
    public final String authorityName;
    public final String description;

    Role(String authorityName,String description) {
        this.authorityName = authorityName;
        this.description = description;
    }
}
