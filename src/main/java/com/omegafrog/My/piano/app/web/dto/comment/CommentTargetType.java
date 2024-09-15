package com.omegafrog.My.piano.app.web.dto.comment;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;

public enum CommentTargetType {
    POST("posts"), VIDEO_POST("video-post"), SHEET_POST("sheet-post"), LESSON("lessons");

    public final String resource;

    CommentTargetType(String resource) {
        this.resource = resource;
    }

    public static CommentTargetType of(HttpServletRequest request) {
        String[] split = request.getRequestURI().split("/");
        return Arrays.stream(CommentTargetType.values()).filter(target ->
                        Arrays.stream(split).anyMatch(requestStr -> requestStr.equals(target.resource))).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Wrong comment target type."));

    }
}
