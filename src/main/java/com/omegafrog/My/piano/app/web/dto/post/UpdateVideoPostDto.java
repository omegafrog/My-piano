package com.omegafrog.My.piano.app.web.dto.post;

import lombok.Builder;
import lombok.Getter;

@Getter
public class UpdateVideoPostDto {
     private int viewCount;

     private String title;

     private String content;

     private String videoUrl;

     @Builder
    public UpdateVideoPostDto(int viewCount, String title, String content, String videoUrl) {
        this.viewCount = viewCount;
        this.title = title;
        this.content = content;
        this.videoUrl = videoUrl;
    }
}
