package com.omegafrog.My.piano.app.user.vo;

public enum LoginMethod {

    EMAIL("email로 로그인"),
    NAVER("naver로 로그인"),
    GOOGLE("구글로 로그인"),
    FACEBOOK("facebook으로 로그인");

    final String description;

    LoginMethod(String description) {
        this.description = description;
    }
}
