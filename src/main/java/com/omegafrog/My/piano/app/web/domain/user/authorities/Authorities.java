package com.omegafrog.My.piano.app.web.domain.user.authorities;

public enum Authorities {
    WRITE_POST("커뮤니티 글 작성"),
    READ_POST("커뮤니티 글 조회"),
    DELETE_POST("커뮤니티 글 삭제"),
    WRITE_SHEET("악보 추가"),
    READ_SHEET("악보 조회"),
    DELETE_SHEET("악보 삭제"),
    WRITE_LESSON("레슨 추가"),
    READ_LESSON("레슨 조회"),
    DELETE_LESSON("레슨 삭제");
    final String description;

    Authorities(String description) {
        this.description = description;
    }
}
