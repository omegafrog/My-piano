package com.omegafrog.My.piano.app.web.dto.comment;

import jakarta.servlet.http.HttpServletRequest;

public enum CommentTargetType {
    POST("post"),VIDEO_POST("video-post"),SHEET_POST("sheet-post"),LESSON("lesson");

    public String resource;

    CommentTargetType(String resource) {
        this.resource = resource;
    }

    public static CommentTargetType of(HttpServletRequest request){
        String[] split = request.getRequestURI().split("/");
        return switch (split[1]) {
            case "posts" -> CommentTargetType.POST;
            case "video-post" -> CommentTargetType.VIDEO_POST;
            case "sheet-post" -> CommentTargetType.SHEET_POST;
            case "lesson" -> CommentTargetType.LESSON;
            default -> throw new IllegalArgumentException("Invalid comment target type");
        };
    }
}
